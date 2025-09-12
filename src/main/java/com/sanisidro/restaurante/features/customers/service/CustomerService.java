package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.audit.service.AuditLogService;
import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.CustomerAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.InvalidPointsOperationException;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.dto.customer.request.CustomerRequest;
import com.sanisidro.restaurante.features.customers.dto.customer.response.CustomerResponse;
import com.sanisidro.restaurante.features.customers.dto.pointshistory.response.PointsHistoryResponse;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.PointsHistory;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.PointsHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;
    private final PointsHistoryRepository pointsHistoryRepository;

    /**
     * Obtiene todos los clientes paginados.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        log.debug("Se obtuvieron {} clientes", content.size());

        return buildPagedResponse(page, content);
    }


    /**
     * Obtiene un cliente por su ID.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        Customer customer = findCustomerById(id);
        log.debug("Cliente obtenido: {}", customer.getId());
        return mapToResponse(customer);
    }

    /**
     * Crea un cliente y aplica la regla de lealtad "PRIMER_REGISTRO".
     */
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (customerRepository.existsByUser_Id(user.getId())) {
            throw new CustomerAlreadyExistsException("El usuario ya tiene un cliente asociado");
        }

        Customer customer = Customer.builder()
                .user(user)
                .points(safePoints(dto.getPoints()))
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Cliente creado: {} ({})", saved.getId(), user.getUsername());

        applyEvent(saved.getId(), "PRIMER_REGISTRO", null, 1);

        return mapToResponse(saved);
    }

    /**
     * Actualiza los puntos del cliente y su información.
     */
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest dto) {
        Customer customer = findCustomerById(id);

        if (dto.getPoints() != null && dto.getPoints() < 0) {
            throw new IllegalArgumentException("Los puntos no pueden ser negativos");
        }

        customer.setPoints(safePoints(dto.getPoints() != null ? dto.getPoints() : customer.getPoints()));
        Customer updated = customerRepository.save(customer);
        log.info("Cliente actualizado: {}", updated.getId());
        return mapToResponse(updated);
    }

    /**
     * Elimina un cliente.
     */
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        customerRepository.deleteById(id);
        log.info("Cliente eliminado: {}", id);
    }


    /**
     * Suma puntos manualmente a un cliente.
     */
    @Transactional
    public CustomerResponse addPoints(Long customerId, int points) {
        if (points <= 0) throw new InvalidPointsOperationException("No se pueden agregar puntos <= 0");

        Customer customer = findCustomerByIdWithLock(customerId);
        customer.setPoints(customer.getPoints() + points);
        Customer saved = customerRepository.save(customer);

        savePointsHistory(saved, points, "MANUAL_ADD");
        log.info("Se agregaron {} puntos al cliente {}", points, customerId);
        return mapToResponse(saved);
    }


    /**
     * Resta puntos manualmente a un cliente, sin que queden negativos.
     */
    @Transactional
    public CustomerResponse subtractPoints(Long customerId, int points) {
        if (points <= 0) throw new InvalidPointsOperationException("No se pueden restar puntos <= 0");

        Customer customer = findCustomerByIdWithLock(customerId);
        int newPoints = Math.max(0, customer.getPoints() - points);
        int subtracted = customer.getPoints() - newPoints;

        customer.setPoints(newPoints);
        Customer saved = customerRepository.save(customer);

        savePointsHistory(saved, -subtracted, "MANUAL_SUBTRACT");
        log.info("Se restaron {} puntos al cliente {}", subtracted, customerId);
        return mapToResponse(saved);
    }

    /**
     * Obtiene los puntos actuales de un cliente.
     */
    @Transactional(readOnly = true)
    public int getPoints(Long customerId) {
        Customer customer = findCustomerById(customerId);
        return customer.getPoints();
    }


    /**
     * Aplica un evento de lealtad y guarda historial.
     */
    @Transactional
    public int applyEvent(Long customerId, String event, Double purchaseAmount, int numberOfPeople) {
        if (!loyaltyService.isValidEvent(event)) {
            throw new InvalidPointsOperationException("Evento de lealtad inválido: " + event);
        }

        Customer customer = findCustomerByIdWithLock(customerId);
        int pointsAdded = loyaltyService.calculatePoints(customer, purchaseAmount, event, numberOfPeople);
        if (pointsAdded > 0) {
            customer.setPoints(customer.getPoints() + pointsAdded);
            customerRepository.save(customer);
            savePointsHistory(customer, pointsAdded, event);
            log.info("Evento {} aplicado: +{} puntos al cliente {}", event, pointsAdded, customerId);
        }
        return pointsAdded;
    }

    /**
     * Obtiene el historial de puntos de un cliente con paginación.
     */
    @Transactional(readOnly = true)
    public PagedResponse<PointsHistoryResponse> getPointsHistory(Long customerId, Pageable pageable) {
        Customer customer = findCustomerById(customerId);
        Page<PointsHistory> page = pointsHistoryRepository.findByCustomerId(customer.getId(), pageable);

        List<PointsHistoryResponse> content = page.getContent().stream()
                .map(ph -> PointsHistoryResponse.builder()
                        .points(ph.getPoints())
                        .event(ph.getEvent())
                        .createdAt(ph.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        log.debug("Historial de puntos obtenido para cliente {}", customer.getId());
        return buildPagedResponse(page, content);
    }


    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private Customer findCustomerByIdWithLock(Long id) {
        return customerRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private void savePointsHistory(Customer customer, int points, String event) {
        PointsHistory history = PointsHistory.builder()
                .customer(customer)
                .points(points)
                .event(event)
                .createdAt(LocalDateTime.now())
                .build();
        pointsHistoryRepository.save(history);
    }

    private int safePoints(Integer points) {
        return points != null && points >= 0 ? points : 0;
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUser().getId())
                .fullName(customer.getUser().getFullName())
                .email(customer.getUser().getEmail())
                .points(customer.getPoints())
                .build();
    }

    private <T> PagedResponse<T> buildPagedResponse(Page<?> page, List<T> content) {
        return PagedResponse.<T>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
