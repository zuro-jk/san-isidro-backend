package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.request.OrderDetailInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.response.OrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.OrderDetail;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepository;
    private final OrderStatusRepository statusRepository;
    private final OrderTypeRepository typeRepository;
    private final ProductRepository productRepository;
    private final DocumentService documentService;
    private final PaymentService paymentService;

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + request.getCustomerId()));

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + request.getEmployeeId()));
        }

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada con id: " + request.getAddressId()));
        }

        OrderStatus status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + request.getStatusId()));

        OrderType type = typeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + request.getTypeId()));

        Order order = Order.builder()
                .customer(customer)
                .employee(employee)
                .address(address)
                .date(LocalDateTime.now())
                .status(status)
                .type(type)
                .total(BigDecimal.ZERO)
                .build();

        Set<OrderDetail> details = new LinkedHashSet<>();
        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + d.getProductId()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .build();

            details.add(detail);
        }
        order.getDetails().addAll(details);

        if (request.getPayments() != null) {
            request.getPayments().forEach(p -> paymentService.createInOrder(order, p));
        }

        if (request.getDocuments() != null) {
            request.getDocuments().forEach(d -> documentService.createInOrder(order, d));
        }

        BigDecimal total = details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + request.getCustomerId()));

        Employee employee = null;
        if (request.getEmployeeId() != null) {
            employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + request.getEmployeeId()));
        }

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new EntityNotFoundException("Dirección no encontrada con id: " + request.getAddressId()));
        }

        OrderStatus status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException("Estado de orden no encontrado con id: " + request.getStatusId()));

        OrderType type = typeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Tipo de orden no encontrado con id: " + request.getTypeId()));

        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setAddress(address);
        order.setStatus(status);
        order.setType(type);
        order.setDate(LocalDateTime.now());

        order.getDetails().clear();
        Set<OrderDetail> details = new LinkedHashSet<>();
        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + d.getProductId()));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .build();
            details.add(detail);
        }
        order.getDetails().addAll(details);

        order.getPayments().clear();
        if (request.getPayments() != null) {
            request.getPayments().forEach(p -> paymentService.createInOrder(order, p));
        }

        order.getDocuments().clear();
        if (request.getDocuments() != null) {
            request.getDocuments().forEach(d -> documentService.createInOrder(order, d));
        }

        BigDecimal total = order.getDetails().stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total);

        return mapToResponse(orderRepository.save(order));
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Orden no encontrada con id: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getUser().getFullName())
                .employeeId(order.getEmployee() != null ? order.getEmployee().getId() : null)
                .employeeName(order.getEmployee() != null ? order.getEmployee().getUser().getFullName() : null)
                .addressId(order.getAddress() != null ? order.getAddress().getId() : null)
                .addressDescription(order.getAddress() != null ? order.getAddress().getDescription() : null)
                .date(order.getDate())
                .statusId(order.getStatus().getId())
                .statusName(order.getStatus().getName())
                .typeId(order.getType().getId())
                .typeName(order.getType().getName())
                .total(order.getTotal())
                .details(order.getDetails().stream()
                        .map(d -> OrderDetailInOrderResponse.builder()
                                .id(d.getId())
                                .productId(d.getProduct().getId())
                                .productName(d.getProduct().getName())
                                .quantity(d.getQuantity())
                                .unitPrice(d.getUnitPrice())
                                .build())
                        .toList())
                .build();
    }

}
