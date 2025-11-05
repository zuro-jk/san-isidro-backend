package com.sanisidro.restaurante.features.customers.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.dto.response.PagedResponse;
import com.sanisidro.restaurante.core.exceptions.CustomerAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.dto.customer.request.CustomerRequest;
import com.sanisidro.restaurante.features.customers.dto.customer.response.CustomerResponse;
import com.sanisidro.restaurante.features.customers.dto.pointshistory.response.PointsHistoryResponse;
import com.sanisidro.restaurante.features.customers.enums.PointHistoryEventType;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.PointsHistory;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;
    private final PointsHistoryService pointsHistoryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> getAllCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        log.debug("Se obtuvieron {} clientes", content.size());
        return buildPagedResponse(page, content);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Long id) {
        Customer customer = findCustomerById(id);
        log.debug("Cliente obtenido: {}", customer.getId());
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> searchCustomersByQuery(String query) {
        log.debug("Buscando clientes con query: {}", query);

        List<Customer> customers = customerRepository.searchByQuery(query.toLowerCase());

        log.debug("Se encontraron {} clientes para la búsqueda: {}", customers.size(), query);

        // 2. Reutiliza tu 'mapToResponse'
        return customers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest dto) {
        User user;

        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        } else {
            user = User.builder()
                    .username(dto.getEmail())
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .email(dto.getEmail())
                    .phone(dto.getPhone())
                    .provider(AuthProvider.LOCAL)
                    .enabled(true)
                    .emailVerified(true)
                    .build();
            userRepository.save(user);
        }

        if (customerRepository.existsByUser_Id(user.getId())) {
            throw new CustomerAlreadyExistsException("El usuario ya tiene un cliente asociado");
        }

        Customer customer = Customer.builder()
                .user(user)
                .points(dto.getPoints() != null ? dto.getPoints() : 0)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Cliente creado: {} ({})", saved.getId(), user.getUsername());

        int points = loyaltyService.calculatePoints(saved, null, "Primer registro", 1);
        pointsHistoryService.applyPoints(saved, points, PointHistoryEventType.EARNING);

        return mapToResponse(saved);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest dto) {
        Customer customer = findCustomerById(id);
        User user = customer.getUser();

        if (dto.getFirstName() != null)
            user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)
            user.setLastName(dto.getLastName());
        if (dto.getEmail() != null)
            user.setEmail(dto.getEmail());
        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());

        userRepository.save(user);
        customerRepository.save(customer);

        log.info("Cliente actualizado: {} ({})", customer.getId(), user.getUsername());
        return mapToResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado");
        }
        customerRepository.deleteById(id);
        log.info("Cliente eliminado: {}", id);
    }

    /* -------------------- Puntos -------------------- */

    @Transactional
    public CustomerResponse applyEvent(Long customerId, String eventName, Double purchaseAmount, int numberOfPeople) {
        Customer customer = findCustomerById(customerId);

        int points = loyaltyService.calculatePoints(customer, purchaseAmount, eventName, numberOfPeople);
        if (points > 0) {
            pointsHistoryService.applyPoints(customer, points, PointHistoryEventType.EARNING);
            log.info("Evento {} aplicado: +{} puntos al cliente {}", eventName, points, customerId);
        }

        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse addPoints(Long customerId, int points) {
        Customer customer = findCustomerById(customerId);
        pointsHistoryService.applyPoints(customer, points, PointHistoryEventType.EARNING);
        log.info("Se agregaron {} puntos al cliente {}", points, customerId);
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse subtractPoints(Long customerId, int points) {
        Customer customer = findCustomerById(customerId);
        pointsHistoryService.applyPoints(customer, -points, PointHistoryEventType.REDEMPTION);
        log.info("Se restaron {} puntos al cliente {}", points, customerId);
        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public int getPoints(Long customerId) {
        return findCustomerById(customerId).getPoints();
    }

    @Transactional(readOnly = true)
    public PagedResponse<PointsHistoryResponse> getPointsHistory(Long customerId, Pageable pageable) {
        Customer customer = findCustomerById(customerId);
        Page<PointsHistory> page = pointsHistoryService.getHistoryByCustomer(customer, pageable);

        List<PointsHistoryResponse> content = page.getContent().stream()
                .map(ph -> PointsHistoryResponse.builder()
                        .points(ph.getPoints())
                        .event(ph.getEvent().name())
                        .createdAt(ph.getCreatedAt())
                        .build())
                .toList();

        log.debug("Historial de puntos obtenido para cliente {}", customer.getId());
        return buildPagedResponse(page, content);
    }

    @Transactional(readOnly = true)
    public long countAllCustomers() {
        return customerRepository.count();
    }

    /* -------------------- Helpers -------------------- */

    private Customer findCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        User user = customer.getUser();

        String profileImageUrl = null;
        if (user.getProfileImageId() != null) {
            profileImageUrl = fileService.getFileUrl(user.getProfileImageId());
        }

        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .points(customer.getPoints())

                // Datos extra del usuario
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles().stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()))
                .provider(user.getProvider().name())
                .profileImageUrl(profileImageUrl)
                .lastUsernameChange(user.getLastUsernameChange())
                .lastEmailChange(user.getLastEmailChange())
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
