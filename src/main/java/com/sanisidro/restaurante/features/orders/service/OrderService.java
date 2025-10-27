package com.sanisidro.restaurante.features.orders.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.config.TaxConfig;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.notifications.dto.OrderNotificationEvent;
import com.sanisidro.restaurante.features.notifications.dto.StockLowNotificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;
import com.sanisidro.restaurante.features.notifications.templates.EmailTemplateBuilder;
import com.sanisidro.restaurante.features.orders.dto.order.request.DeliveryAddressRequest;
import com.sanisidro.restaurante.features.orders.dto.order.request.OrderRequest;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderCreatedEvent;
import com.sanisidro.restaurante.features.orders.dto.order.response.OrderResponse;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.request.OrderDetailInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.response.OrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.OrderDetail;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderStatusTranslation;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.products.enums.MovementSource;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import com.sanisidro.restaurante.features.products.exceptions.InventoryNotFoundException;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.model.InventoryMovement;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.model.ProductIngredient;
import com.sanisidro.restaurante.features.products.repository.InventoryMovementRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import com.sanisidro.restaurante.features.reports.dto.response.OrderTypeReportResponse;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.Store;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.StoreRepository;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;

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
        private final OrderStatusRepository statusRepository;
        private final OrderTypeRepository typeRepository;
        private final ProductRepository productRepository;
        private final InventoryRepository inventoryRepository;
        private final InventoryMovementRepository inventoryMovementRepository;
        private final DocumentService documentService;
        private final PaymentService paymentService;
        private final NotificationProducer notificationProducer;
        private final UserRepository userRepository;
        private final TableRepository tableRepository;
        private final StoreRepository storeRepository;
        private final DistanceMatrixService distanceMatrixService;

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

        public OrderResponse getTrackingInfo(Long id, String lang) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + id));

                if (order.getType() != null &&
                                "DELIVERY".equalsIgnoreCase(order.getType().getCode()) &&
                                order.getDeliveryLatitude() != null && order.getDeliveryLongitude() != null) {

                        try {
                                Store mainStore = storeRepository.findAll().stream()
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalStateException(
                                                                "No hay tiendas configuradas."));

                                double originLat = mainStore.getLatitude();
                                double originLng = mainStore.getLongitude();
                                double destLat = order.getDeliveryLatitude();
                                double destLng = order.getDeliveryLongitude();

                                // Consultar servicio DistanceMatrix (por ejemplo, Google API)
                                Map<String, Object> distanceResponse = distanceMatrixService
                                                .getDistanceAndDuration(originLat, originLng, destLat, destLng);

                                var rows = (List<?>) distanceResponse.get("rows");
                                if (rows != null && !rows.isEmpty()) {
                                        var elements = (List<?>) ((Map<?, ?>) rows.get(0)).get("elements");
                                        if (elements != null && !elements.isEmpty()) {
                                                Map<?, ?> element = (Map<?, ?>) elements.get(0);
                                                Map<?, ?> distance = (Map<?, ?>) element.get("distance");
                                                Map<?, ?> duration = (Map<?, ?>) element.get("duration");

                                                if (distance != null && duration != null) {
                                                        order.setEstimatedDistance(distance.get("text").toString());
                                                        order.setEstimatedDuration(duration.get("text").toString());
                                                }
                                        }
                                }
                        } catch (Exception e) {
                                log.error("Error recalculando distancia o duración estimada para tracking: {}",
                                                e.getMessage());
                        }
                }

                orderRepository.save(order);

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
        public OrderResponse create(User user, OrderRequest request, String lang) {
                Customer customer = customerRepository.findByUserId(user.getId())
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
                                throw new IllegalStateException(
                                                "El producto con id " + product.getId() + " no tiene precio asignado.");
                        }

                        BigDecimal taxRate = taxConfig.getRate();
                        if (taxRate == null) {
                                throw new IllegalStateException("No está configurada la tasa de impuestos.");
                        }

                        BigDecimal priceWithTax = basePrice.multiply(BigDecimal.ONE.add(taxRate));
                        BigDecimal safeQuantity = d.getQuantity() != null ? BigDecimal.valueOf(d.getQuantity())
                                        : BigDecimal.ZERO;

                        if (safeQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                                throw new IllegalArgumentException(
                                                "Cantidad inválida para el producto con id " + d.getProductId());
                        }

                        BigDecimal lineTotal = priceWithTax.multiply(safeQuantity);

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

                if (order.getType() != null &&
                                "DELIVERY".equalsIgnoreCase(order.getType().getCode()) &&
                                order.getDeliveryLatitude() != null &&
                                order.getDeliveryLongitude() != null) {

                        try {
                                Store mainStore = storeRepository.findAll().stream()
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalStateException(
                                                                "No hay tiendas configuradas."));

                                double originLat = mainStore.getLatitude();
                                double originLng = mainStore.getLongitude();
                                double destLat = order.getDeliveryLatitude();
                                double destLng = order.getDeliveryLongitude();

                                Map<String, Object> distanceResponse = distanceMatrixService.getDistanceAndDuration(
                                                originLat, originLng, destLat, destLng);

                                var rows = (List<?>) distanceResponse.get("rows");
                                if (rows != null && !rows.isEmpty()) {
                                        var elements = (List<?>) ((Map<?, ?>) rows.get(0)).get("elements");
                                        if (elements != null && !elements.isEmpty()) {
                                                Map<?, ?> element = (Map<?, ?>) elements.get(0);
                                                Map<?, ?> distance = (Map<?, ?>) element.get("distance");
                                                Map<?, ?> duration = (Map<?, ?>) element.get("duration");

                                                if (distance != null && duration != null) {
                                                        String distanceText = distance.get("text").toString();
                                                        String durationText = duration.get("text").toString();

                                                        log.info("Distancia calculada: {}, Duración estimada: {}",
                                                                        distanceText, durationText);

                                                        order.setEstimatedDistance(distanceText);
                                                        order.setEstimatedDuration(durationText);

                                                        int estimatedTimeMinutes = parseDurationToMinutes(durationText);

                                                        int totalPrepTime = request.getDetails().stream()
                                                                        .mapToInt(d -> {
                                                                                Product p = productRepository.findById(
                                                                                                d.getProductId())
                                                                                                .orElse(null);
                                                                                return p != null && p
                                                                                                .getPreparationTimeMinutes() != null
                                                                                                                ? p.getPreparationTimeMinutes()
                                                                                                                : 0;
                                                                        })
                                                                        .sum();

                                                        int totalItems = request.getDetails().stream()
                                                                        .mapToInt(d -> d.getQuantity() != null
                                                                                        ? d.getQuantity()
                                                                                        : 0)
                                                                        .sum();

                                                        int baseDelay = 10;
                                                        int itemDelay = totalItems * 2;
                                                        int trafficDelay = Math.max(5,
                                                                        (int) Math.round(estimatedTimeMinutes * 0.3));

                                                        int estimatedTotalTime = totalPrepTime + estimatedTimeMinutes
                                                                        + baseDelay + itemDelay + trafficDelay;

                                                        order.setEstimatedTime(estimatedTotalTime);
                                                }
                                        }
                                }
                        } catch (Exception e) {
                                log.error("Error al calcular distancia o duración estimada: {}", e.getMessage());
                                order.setEstimatedTime(30);
                        }
                }

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

                if (request.getDeliveryAddress() != null) {
                        DeliveryAddressRequest addrDto = request.getDeliveryAddress();
                        order.setDeliveryStreet(addrDto.getStreet());
                        order.setDeliveryReference(addrDto.getReference());
                        order.setDeliveryCity(addrDto.getCity());
                        order.setDeliveryInstructions(addrDto.getInstructions());
                        order.setDeliveryProvince(addrDto.getProvince());
                        order.setDeliveryZipCode(addrDto.getZipCode());
                        order.setDeliveryLatitude(addrDto.getLatitude());
                        order.setDeliveryLongitude(addrDto.getLongitude());
                }

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

        public OrderResponse cancelOrder(Long id, User user, String lang) {
                Order order = orderRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada"));

                if (order.getStatus().getCode().equalsIgnoreCase("CANCELLED")) {
                        throw new IllegalStateException("La orden ya se encuentra cancelada.");
                }

                boolean isOwner = order.getCustomer() != null
                                && order.getCustomer().getUser() != null
                                && order.getCustomer().getUser().getId().equals(user.getId());

                boolean isAdmin = user.getRoles().stream()
                                .anyMatch(role -> role.getName().equalsIgnoreCase("ROLE_ADMIN"));

                if (!isOwner && !isAdmin) {
                        throw new SecurityException("El usuario no está autorizado para cancelar esta orden.");
                }

                OrderStatus cancelledStatus = statusRepository.findByCode("CANCELLED")
                                .orElseThrow(() -> new EntityNotFoundException("Estado 'CANCELLED' no encontrado"));
                order.setStatus(cancelledStatus);

                if (order.getDetails() != null && !order.getDetails().isEmpty()) {
                        for (OrderDetail detail : order.getDetails()) {
                                Product product = detail.getProduct();
                                if (product != null && product.getIngredients() != null) {
                                        for (ProductIngredient productIngredient : product.getIngredients()) {
                                                Ingredient ingredient = productIngredient.getIngredient();
                                                BigDecimal ingredientQty = BigDecimal
                                                                .valueOf(productIngredient.getQuantity())
                                                                .multiply(BigDecimal.valueOf(detail.getQuantity()));
                                                Inventory inventory = inventoryRepository.findByIngredient(ingredient)
                                                                .orElse(null);
                                                if (inventory != null) {
                                                        inventory.increaseStock(ingredientQty);
                                                        inventoryRepository.save(inventory);
                                                }
                                        }
                                }
                        }
                }

                orderRepository.save(order);

                // TODO: Enviar notificación ?
                // notificationProducer.sendOrderCancelledNotification(order);

                return mapToResponse(order, lang);
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

                OrderStatus status = statusRepository.findById(request.getStatusId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Estado de orden no encontrado con id: " + request.getStatusId()));
                OrderType type = typeRepository.findById(request.getTypeId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Tipo de orden no encontrado con id: " + request.getTypeId()));

                Order.OrderBuilder orderBuilder = Order.builder()
                                .customer(customer)
                                .employee(employee)
                                .status(status)
                                .type(type)
                                .date(LocalDateTime.now())
                                .total(BigDecimal.ZERO)
                                .details(new LinkedHashSet<>())
                                .payments(new LinkedHashSet<>())
                                .documents(new LinkedHashSet<>());

                String orderTypeCode = type.getCode() != null ? type.getCode().toUpperCase() : "";

                switch (orderTypeCode) {
                        case "DELIVERY":
                                if (request.getDeliveryAddress() == null) {
                                        throw new IllegalArgumentException(
                                                        "La dirección de entrega es obligatoria para órdenes a domicilio.");
                                }

                                DeliveryAddressRequest addrDto = request.getDeliveryAddress();
                                orderBuilder.deliveryStreet(addrDto.getStreet())
                                                .deliveryReference(addrDto.getReference())
                                                .deliveryCity(addrDto.getCity())
                                                .deliveryInstructions(addrDto.getInstructions())
                                                .deliveryProvince(addrDto.getProvince())
                                                .deliveryZipCode(addrDto.getZipCode())
                                                .deliveryLatitude(addrDto.getLatitude())
                                                .deliveryLongitude(addrDto.getLongitude());

                                Store mainStore = storeRepository.findAll().stream()
                                                .findFirst()
                                                .orElseThrow(() -> new IllegalStateException(
                                                                "No hay tiendas configuradas en el sistema."));

                                double originLat = mainStore.getLatitude();
                                double originLng = mainStore.getLongitude();
                                double destLat = addrDto.getLatitude();
                                double destLng = addrDto.getLongitude();

                                try {
                                        Map<String, Object> distanceResponse = distanceMatrixService
                                                        .getDistanceAndDuration(originLat, originLng, destLat, destLng);

                                        var rows = (List<?>) distanceResponse.get("rows");
                                        if (rows != null && !rows.isEmpty()) {
                                                var elements = (List<?>) ((Map<?, ?>) rows.get(0)).get("elements");
                                                if (elements != null && !elements.isEmpty()) {
                                                        Map<?, ?> element = (Map<?, ?>) elements.get(0);
                                                        Map<?, ?> distance = (Map<?, ?>) element.get("distance");
                                                        Map<?, ?> duration = (Map<?, ?>) element.get("duration");

                                                        if (distance != null && duration != null) {
                                                                log.info("Distancia: {}, Duración: {}",
                                                                                distance.get("text"),
                                                                                duration.get("text"));

                                                                orderBuilder
                                                                                .estimatedDistance(distance.get("text")
                                                                                                .toString())
                                                                                .estimatedDuration(duration.get("text")
                                                                                                .toString());
                                                        }
                                                }
                                        }
                                } catch (Exception e) {
                                        log.error("Error al obtener la distancia o duración estimada: {}",
                                                        e.getMessage());
                                }

                                break;

                        case "DINE_IN":
                                if (request.getTableId() == null) {
                                        throw new IllegalArgumentException(
                                                        "La mesa es obligatoria para órdenes presenciales.");
                                }
                                TableEntity selectedTable = tableRepository.findById(request.getTableId())
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                "Mesa no encontrada con id: " + request.getTableId()));

                                if (selectedTable.getStatus() != TableStatus.FREE) {
                                        throw new IllegalStateException(
                                                        "La mesa " + selectedTable.getCode() + " no está disponible.");
                                }

                                orderBuilder.table(selectedTable);
                                break;

                        case "TAKE_AWAY":
                                if (request.getPickupStoreId() == null) {
                                        throw new IllegalArgumentException(
                                                        "La tienda de recogida es obligatoria para órdenes para llevar.");
                                }

                                Store pickupStore = storeRepository.findById(request.getPickupStoreId())
                                                .orElseThrow(() -> new EntityNotFoundException(
                                                                "Tienda no encontrada con id: "
                                                                                + request.getPickupStoreId()));

                                orderBuilder.pickupStore(pickupStore);
                                break;

                        default:
                                log.warn("Tipo de orden no reconocido para lógica especial: {}", orderTypeCode);
                                break;
                }

                return orderBuilder.build();
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
                OrderResponse.OrderResponseBuilder responseBuilder = OrderResponse.builder()
                                .id(order.getId())
                                .customerId(order.getCustomer().getId())
                                .customerName(order.getCustomer().getUser().getFullName())
                                .employeeId(order.getEmployee() != null ? order.getEmployee().getId() : null)
                                .employeeName(order.getEmployee() != null ? order.getEmployee().getUser().getFullName()
                                                : null)
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
                                .estimatedTime(order.getEstimatedTime())
                                .estimatedDistance(order.getEstimatedDistance())
                                .estimatedDuration(order.getEstimatedDuration())
                                .currentLatitude(order.getCurrentLatitude())
                                .currentLongitude(order.getCurrentLongitude());
                ;

                String orderTypeCode = order.getType().getCode() != null ? order.getType().getCode().toUpperCase() : "";

                switch (orderTypeCode) {
                        case "DELIVERY":
                                responseBuilder
                                                .deliveryStreet(order.getDeliveryStreet())
                                                .deliveryReference(order.getDeliveryReference())
                                                .deliveryCity(order.getDeliveryCity())
                                                .deliveryInstructions(order.getDeliveryInstructions())
                                                .deliveryProvince(order.getDeliveryProvince())
                                                .deliveryZipCode(order.getDeliveryZipCode())
                                                .deliveryLatitude(order.getDeliveryLatitude())
                                                .deliveryLongitude(order.getDeliveryLongitude());
                                break;

                        case "DINE_IN":
                                if (order.getTable() != null) {
                                        responseBuilder.tableId(order.getTable().getId())
                                                        .tableCode(order.getTable().getCode());
                                }
                                break;
                        case "TAKE_AWAY":
                                if (order.getPickupStore() != null) {
                                        responseBuilder
                                                        .pickupStoreId(order.getPickupStore().getId())
                                                        .pickupStoreName(order.getPickupStore().getName())
                                                        .pickupStoreAddress(order.getPickupStore().getAddress());
                                }
                                break;
                }

                return responseBuilder.build();
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
                if (lang == null || lang.isEmpty())
                        return "es";
                if (lang.startsWith("es"))
                        return "es";
                if (lang.startsWith("en"))
                        return "en";
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

        private int parseDurationToMinutes(String durationText) {
                if (durationText == null || durationText.isBlank())
                        return 0;

                durationText = durationText.toLowerCase();
                int minutes = 0;

                try {
                        if (durationText.contains("hour")) {
                                String[] parts = durationText.split("hour");
                                int hours = Integer.parseInt(parts[0].trim());
                                minutes += hours * 60;

                                if (parts.length > 1 && parts[1].contains("min")) {
                                        String mins = parts[1].replaceAll("[^0-9]", "");
                                        if (!mins.isEmpty())
                                                minutes += Integer.parseInt(mins);
                                }
                        } else if (durationText.contains("min")) {
                                String mins = durationText.replaceAll("[^0-9]", "");
                                if (!mins.isEmpty())
                                        minutes = Integer.parseInt(mins);
                        }
                } catch (Exception e) {
                        log.warn("No se pudo parsear duración estimada: {}", durationText);
                }

                return minutes;
        }

        public int countOrdersByDate(LocalDate date) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);
                return orderRepository.countOrdersByDate(startOfDay, endOfDay);
        }

        public BigDecimal calculateSalesByDate(LocalDate date) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(23, 59, 59);
                return orderRepository.calculateSalesByDate(startOfDay, endOfDay);
        }

        public Map<LocalDate, Integer> countOrdersLast7Days() {
                Map<LocalDate, Integer> map = new LinkedHashMap<>();
                for (int i = 6; i >= 0; i--) {
                        LocalDate day = LocalDate.now().minusDays(i);
                        LocalDateTime startOfDay = day.atStartOfDay();
                        LocalDateTime endOfDay = day.atTime(23, 59, 59);
                        int count = orderRepository.countOrdersByDate(startOfDay, endOfDay);
                        map.put(day, count);
                }
                return map;
        }

        public Map<LocalDate, BigDecimal> calculateSalesLast7Days() {
                Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
                for (int i = 6; i >= 0; i--) {
                        LocalDate day = LocalDate.now().minusDays(i);
                        LocalDateTime startOfDay = day.atStartOfDay();
                        LocalDateTime endOfDay = day.atTime(23, 59, 59);
                        BigDecimal sales = orderRepository.calculateSalesByDate(startOfDay, endOfDay);
                        map.put(day, sales != null ? sales : BigDecimal.ZERO);
                }
                return map;
        }

        public Long countAllOrders() {
                return orderRepository.count();
        }

        public BigDecimal calculateTotalSales() {
                List<String> validStatuses = List.of("COMPLETED", "DELIVERED", "PAID");
                BigDecimal total = orderRepository.sumTotalByStatusCodes(validStatuses);
                return total != null ? total : BigDecimal.ZERO;
        }

        public List<OrderTypeReportResponse> getOrderTypeStatistics(String lang) {
                return orderRepository.findOrderTypeStatistics(lang)
                                .stream()
                                .map(rowObj -> {
                                        Object[] row = (Object[]) rowObj;

                                        String orderTypeName = row[0] != null ? row[0].toString() : "DESCONOCIDO";
                                        Long totalOrders = ((Number) row[1]).longValue();
                                        BigDecimal totalRevenue = row[2] instanceof BigDecimal
                                                        ? (BigDecimal) row[2]
                                                        : new BigDecimal(row[2].toString());

                                        return OrderTypeReportResponse.builder()
                                                        .orderTypeName(orderTypeName)
                                                        .totalOrders(totalOrders.intValue())
                                                        .totalRevenue(totalRevenue)
                                                        .build();
                                })
                                .toList();
        }
}
