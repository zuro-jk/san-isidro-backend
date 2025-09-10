package com.sanisidro.restaurante.features.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanisidro.restaurante.core.kafka.message.KafkaMessage;
import com.sanisidro.restaurante.core.kafka.producer.KafkaProducerService;
import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderCreatedEvent;
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

    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;

    public List<OrderResponse> getAll(String lang) {
        return orderRepository.findAll().stream()
                .map(order -> mapToResponse(order, lang))
                .toList();
    }

    public OrderResponse getById(Long id, String lang) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));
        return mapToResponse(order, lang);
    }

    @Transactional
    public OrderResponse create(OrderRequest request, String lang) {
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
                .details(new LinkedHashSet<>())
                .payments(new LinkedHashSet<>())
                .documents(new LinkedHashSet<>())
                .build();

        Set<OrderDetail> details = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + d.getProductId()));

            BigDecimal priceWithTax = product.getPrice()
                    .multiply(BigDecimal.valueOf(1.18));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(priceWithTax)
                    .build();

            details.add(detail);

            total = total.add(priceWithTax.multiply(BigDecimal.valueOf(d.getQuantity())));
        }
        order.setDetails(details);
        order.setTotal(total);

        if (request.getPayments() != null) {
            request.getPayments().forEach(p -> paymentService.createInOrder(order, p));
        }

        if (request.getDocuments() != null) {
            request.getDocuments().forEach(d -> documentService.createInOrder(order, d));
        }

        Order savedOrder = orderRepository.save(order);

        publishOrderCreatedEvent(savedOrder);

        return mapToResponse(savedOrder, lang);
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest request, String lang) {
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

        Set<OrderDetail> updatedDetails = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + d.getProductId()));

            BigDecimal priceWithTax = product.getPrice().multiply(BigDecimal.valueOf(1.18));

            OrderDetail existingDetail = order.getDetails().stream()
                    .filter(detail -> detail.getProduct().getId().equals(product.getId()))
                    .findFirst()
                    .orElse(null);

            if (existingDetail != null) {
                existingDetail.setQuantity(d.getQuantity());
                existingDetail.setUnitPrice(priceWithTax);
                updatedDetails.add(existingDetail);
            } else {
                OrderDetail newDetail = OrderDetail.builder()
                        .order(order)
                        .product(product)
                        .quantity(d.getQuantity())
                        .unitPrice(priceWithTax)
                        .build();
                updatedDetails.add(newDetail);
            }

            total = total.add(priceWithTax.multiply(BigDecimal.valueOf(d.getQuantity())));
        }

        order.getDetails().clear();
        order.getDetails().addAll(updatedDetails);
        order.setTotal(total);

        order.getPayments().clear();
        if (request.getPayments() != null) {
            request.getPayments().forEach(p -> paymentService.createInOrder(order, p));
        }

        order.getDocuments().clear();
        if (request.getDocuments() != null) {
            request.getDocuments().forEach(d -> documentService.createInOrder(order, d));
        }

        return mapToResponse(orderRepository.save(order), lang);
    }

    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new EntityNotFoundException("Orden no encontrada con id: " + id);
        }
        orderRepository.deleteById(id);
    }

    private OrderResponse mapToResponse(Order order, String lang) {
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
                .statusName(getStatusName(order.getStatus(), lang))
                .typeId(order.getType().getId())
                .typeName(getTypeName(order.getType(), lang))
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

    private void publishOrderCreatedEvent(Order savedOrder) {
        try {
            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderId(savedOrder.getId())
                    .customerId(savedOrder.getCustomer().getId())
                    .customerName(savedOrder.getCustomer().getUser().getFullName())
                    .customerEmail(savedOrder.getCustomer().getUser().getEmail())
                    .total(savedOrder.getTotal())
                    .createdAt(savedOrder.getDate())
                    .products(savedOrder.getDetails().stream()
                            .map(d -> OrderCreatedEvent.ProductInfo.builder()
                                    .name(d.getProduct().getName())
                                    .unitPrice(d.getUnitPrice())
                                    .quantity(d.getQuantity())
                                    .build())
                            .toList())
                    .build();

            String payload = objectMapper.writeValueAsString(event);

            kafkaProducerService.sendMessage(
                    KafkaMessage.builder()
                            .topic("orders")
                            .key(savedOrder.getId().toString())
                            .payload(payload)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error serializando evento de orden", e);
        }
    }

    private String getStatusName(OrderStatus status, String lang) {
        return status.getTranslations().stream()
                .filter(t -> t.getLang().equalsIgnoreCase(lang))
                .map(t -> t.getName())
                .findFirst()
                .orElse("Sin nombre");
    }

    private String getTypeName(OrderType type, String lang) {
        return type.getTranslations().stream()
                .filter(t -> t.getLang().equalsIgnoreCase(lang))
                .map(t -> t.getName())
                .findFirst()
                .orElse("Sin nombre");
    }

}
