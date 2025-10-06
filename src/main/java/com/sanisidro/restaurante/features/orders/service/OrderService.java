package com.sanisidro.restaurante.features.orders.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.sanisidro.restaurante.features.orders.model.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.config.TaxConfig;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.model.Address;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.AddressRepository;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.StockLowNotificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderCreatedEvent;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.request.OrderDetailInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.response.OrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.products.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import com.sanisidro.restaurante.features.products.exceptions.InventoryNotFoundException;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.InventoryMovement;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.InventoryMovementRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final TaxConfig taxConfig;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepository;
    private final OrderStatusRepository statusRepository;
    private final OrderTypeRepository typeRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final DocumentService documentService;
    private final PaymentService paymentService;

    private final NotificationProducer notificationProducer;

    private final UserRepository userRepository;

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

    public List<OrderResponse> getOrdersForCurrentUser(User user, String lang) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado para el usuario autenticado"));

        return orderRepository.findByCustomer(customer).stream()
                .map(order -> mapToResponse(order, lang))
                .toList();
    }

    @Transactional
    public OrderResponse create(OrderRequest request, String lang) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Customer customer = customerRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado para el usuario autenticado"));

        Order order = buildOrderBase(request, customer);

        BigDecimal total = BigDecimal.ZERO;
        Set<OrderDetail> details = new LinkedHashSet<>();

        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id: " + d.getProductId()));

            BigDecimal basePrice = product.getPrice();
            if (basePrice == null) {
                throw new IllegalStateException("El producto con id " + product.getId() + " no tiene precio asignado.");
            }

            BigDecimal taxRate = taxConfig.getRate();
            if (taxRate == null) {
                throw new IllegalStateException("No está configurada la tasa de impuestos.");
            }

            BigDecimal priceWithTax = basePrice.multiply(BigDecimal.ONE.add(taxRate));

            BigDecimal safeQuantity = d.getQuantity() != null ? BigDecimal.valueOf(d.getQuantity()) : BigDecimal.ZERO;
            if (safeQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para el producto con id " + d.getProductId());
            }

            BigDecimal lineTotal = priceWithTax.multiply(safeQuantity);
            if (lineTotal == null) {
                throw new IllegalStateException("Error al calcular el total de la línea del producto con id " + product.getId());
            }

            log.debug("Calculando línea -> productoId={}, priceWithTax={}, quantity={}, lineTotal={}",
                    product.getId(), priceWithTax, safeQuantity, lineTotal);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(priceWithTax)
                    .build();

            applyInventoryMovement(product, d.getQuantity(), false, order.getId(), "Creación de orden");

            details.add(detail);
            total = total.add(lineTotal);
        }

        order.setDetails(details);
        order.setTotal(total);

        savePaymentsAndDocuments(order, request);

        Order savedOrder = orderRepository.save(order);
        publishOrderCreatedEvent(savedOrder);

        return mapToResponse(savedOrder, lang);
    }
    @Transactional
    public OrderResponse update(Long id, OrderRequest request, String lang) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));

        order.setCustomer(customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con id: " + request.getCustomerId())));
        order.setEmployee(request.getEmployeeId() != null ? employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Empleado no encontrado con id: " + request.getEmployeeId()))
                : null);
        order.setAddress(request.getAddressId() != null ? addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Dirección no encontrada con id: " + request.getAddressId()))
                : null);
        order.setStatus(statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estado de orden no encontrado con id: " + request.getStatusId())));
        order.setType(typeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tipo de orden no encontrado con id: " + request.getTypeId())));
        order.setDate(LocalDateTime.now());

        order.getDetails().forEach(detail -> applyInventoryMovement(detail.getProduct(), detail.getQuantity(),
                true, order.getId(), "Actualización de orden"));

        Set<OrderDetail> updatedDetails = new LinkedHashSet<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetailInOrderRequest d : request.getDetails()) {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id: " + d.getProductId()));
            BigDecimal priceWithTax = product.getPrice().multiply(BigDecimal.ONE.add(taxConfig.getRate()));
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(priceWithTax)
                    .build();

            applyInventoryMovement(product, d.getQuantity(), false, order.getId(),
                    "Actualización de orden");

            updatedDetails.add(detail);
            total = total.add(priceWithTax.multiply(BigDecimal.valueOf(d.getQuantity())));
        }

        order.getDetails().clear();
        order.getDetails().addAll(updatedDetails);
        order.setTotal(total);

        savePaymentsAndDocuments(order, request);

        return mapToResponse(orderRepository.save(order), lang);
    }

    @Transactional
    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));

        order.getDetails().forEach(detail -> applyInventoryMovement(detail.getProduct(), detail.getQuantity(),
                true, order.getId(), "Eliminación de orden"));

        orderRepository.delete(order);
    }

    @Transactional
    public void addLocalPayment(Long orderId, PaymentInOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden no encontrada con id: " + orderId));

        paymentService.createInOrder(order, request);
        orderRepository.save(order);
    }

    private Order buildOrderBase(OrderRequest request, Customer customer) {
        Employee employee = request.getEmployeeId() != null ? employeeRepository
                .findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Empleado no encontrado con id: " + request.getEmployeeId()))
                : null;
        Address address = request.getAddressId() != null ? addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Dirección no encontrada con id: " + request.getAddressId()))
                : null;
        OrderStatus status = statusRepository.findById(request.getStatusId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Estado de orden no encontrado con id: " + request.getStatusId()));
        OrderType type = typeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Tipo de orden no encontrado con id: " + request.getTypeId()));

        return Order.builder()
                .customer(customer)
                .employee(employee)
                .address(address)
                .status(status)
                .type(type)
                .date(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .details(new LinkedHashSet<>())
                .payments(new LinkedHashSet<>())
                .documents(new LinkedHashSet<>())
                .build();
    }
    private void savePaymentsAndDocuments(Order order, OrderRequest request) {
        order.getPayments().clear();
        if (request.getPayments() != null)
            request.getPayments().forEach(p -> paymentService.createInOrder(order, p));

        order.getDocuments().clear();
        if (request.getDocuments() != null)
            request.getDocuments().forEach(d -> documentService.createInOrder(order, d));
    }

    private void applyInventoryMovement(Product product, Integer quantity, boolean restore, Long referenceId,
                                        String reason) {
        product.getIngredients().forEach(pi -> {
            Inventory inventory = inventoryRepository.findByIngredient(pi.getIngredient())
                    .orElseThrow(() -> new InventoryNotFoundException(
                            "Inventario no encontrado para ingrediente: "
                                    + pi.getIngredient().getName()));
            BigDecimal totalQty = BigDecimal.valueOf(pi.getQuantity())
                    .multiply(BigDecimal.valueOf(quantity));

            if (restore)
                inventory.increaseStock(totalQty);
            else
                inventory.decreaseStock(totalQty);

            inventoryRepository.save(inventory);

            InventoryMovement movement = InventoryMovement.builder()
                    .ingredient(pi.getIngredient())
                    .quantity(totalQty)
                    .type(restore ? MovementType.ENTRY : MovementType.EXIT)
                    .source(MovementSource.ORDER)
                    .reason(reason)
                    .referenceId(referenceId)
                    .date(LocalDateTime.now())
                    .build();
            inventoryMovementRepository.save(movement);

            if (!restore && inventory.getCurrentStock().compareTo(inventory.getMinimumStock()) < 0) {
                if (inventory.shouldNotifyLowStock()) {
                    notifyAdminsStockLowAsync(inventory);
                    inventory.markAlertSent();
                    inventoryRepository.save(inventory);
                }
            }
        });
    }

    private OrderResponse mapToResponse(Order order, String lang) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getUser().getFullName())
                .employeeId(order.getEmployee() != null ? order.getEmployee().getId() : null)
                .employeeName(order.getEmployee() != null ? order.getEmployee().getUser().getFullName()
                        : null)
                .addressId(order.getAddress() != null ? order.getAddress().getId() : null)
                .addressDescription(
                        order.getAddress() != null ? order.getAddress().getDescription() : null)
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
                            .map(item -> OrderCreatedEvent.ProductInfo.builder()
                                    .name(item.getProduct().getName())
                                    .unitPrice(item.getProduct().getPrice())
                                    .quantity(item.getQuantity())
                                    .build())
                            .toList())
                    .build();

            OrderNotificationEvent notification = OrderNotificationEvent.builder()
                    .userId(event.getCustomerId())
                    .recipient(event.getCustomerEmail())
                    .subject("¡Tu orden #" + event.getOrderId() + " ha sido confirmada!")
                    .message("¡Wow! Gracias por tu compra, " + event.getCustomerName()
                            + ". Tu orden está en proceso.")
                    .actionUrl("https://miapp.com/orders/" + event.getOrderId())
                    .orderId(event.getOrderId())
                    .products(event.getProducts().stream()
                            .map(p -> new EmailTemplateBuilder.OrderProduct(
                                    p.getName(),
                                    p.getUnitPrice(),
                                    p.getQuantity()))
                            .toList())
                    .total(event.getTotal())
                    .orderDate(event.getCreatedAt())
                    .build();

            notificationProducer.send("notifications", notification);

        } catch (Exception e) {
            throw new RuntimeException("Error creando evento de notificación de orden", e);
        }
    }

    @Async
    public void notifyAdminsStockLowAsync(Inventory inventory) {
        List<User> admins = userRepository.findByRoleName("ROLE_ADMIN");

        for (User admin : admins) {
            try {
                StockLowNotificationEvent event = StockLowNotificationEvent.builder()
                        .userId(admin.getId())
                        .recipient(admin.getEmail())
                        .subject("⚠️ Stock bajo: " + inventory.getIngredient().getName())
                        .message("El stock del ingrediente '"
                                + inventory.getIngredient().getName() +
                                "' ha bajado a " + inventory.getCurrentStock() +
                                " unidades. Stock mínimo: "
                                + inventory.getMinimumStock())
                        .actionUrl("https://tuapp.com/inventory")
                        .ingredientId(inventory.getIngredient().getId())
                        .ingredientName(inventory.getIngredient().getName())
                        .currentStock(inventory.getCurrentStock())
                        .minimumStock(inventory.getMinimumStock())
                        .build();

                notificationProducer.send("notifications", event);
            } catch (Exception e) {
                log.error("❌ Error enviando notificación de stock bajo para admin {}", admin.getEmail(),
                        e);
            }
        }
    }

    private String normalizeLang(String lang) {
        if (lang == null || lang.isEmpty()) return "es";
        if (lang.startsWith("es")) return "es";
        if (lang.startsWith("en")) return "en";
        return "es";
    }

    private String getStatusName(OrderStatus status, String lang) {
        String normalizedLang = normalizeLang(lang);
        return status.getTranslations().stream()
                .filter(t -> t.getLang().equalsIgnoreCase(normalizedLang))
                .map(OrderStatusTranslation::getName)
                .findFirst()
                .orElse("Sin nombre");
    }

    private String getTypeName(OrderType type, String lang) {
        String normalizedLang = normalizeLang(lang);
        return type.getTranslations().stream()
                .filter(t -> t.getLang().equalsIgnoreCase(normalizedLang))
                .map(OrderTypeTranslation::getName)
                .findFirst()
                .orElse("Sin nombre");
    }

}
