package com.sanisidro.restaurante.features.orders.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.security.model.PaymentProfile;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.PaymentProfileRepository;
import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.OnlineCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentUpdateRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.response.PaymentResponse;
import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.Payment;
import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final MercadoPagoService mercadoPagoService;
    private final PaymentProfileRepository paymentProfileRepository;
    private final OrderStatusRepository orderStatusRepository;

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PaymentResponse getById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));
        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse createOnlinePayment(OnlineCheckoutRequest request) throws Exception {

        log.info("Iniciando createOnlinePayment para orderId: {}", request.getOrderId());

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> {
                    log.warn("Orden no encontrada con id: {}", request.getOrderId());
                    return new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId());
                });

        if (order.getType() == null) {
            throw new IllegalArgumentException("El tipo de orden es nulo.");
        }

        String orderTypeCode = order.getType().getCode().toUpperCase();

        if (!"DELIVERY".equals(orderTypeCode) && !"TAKE_AWAY".equals(orderTypeCode)) {
            log.warn(
                    "Intento de pago online RECHAZADO para una orden que no es 'DELIVERY' o 'TAKE_AWAY'. Order ID: {}, Type: {}",
                    order.getId(), orderTypeCode);

            throw new IllegalArgumentException(
                    "Los pagos online no están permitidos para este tipo de orden (" + orderTypeCode + ").");
        }

        if (order.getTotal() == null || order.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Intento de pago para orden {} con monto inválido (nulo o <= 0): {}",
                    order.getId(), order.getTotal());
            throw new IllegalArgumentException("El monto a pagar (calculado en la orden) debe ser mayor que cero.");
        }

        log.info("Monto recibido (request): {}. Monto a procesar (order.getTotal): {}",
                request.getTransactionAmount(), order.getTotal());

        PaymentMethod method = paymentMethodRepository.findByCodeAndProvider("CARD", "MERCADOPAGO")
                .orElseThrow(() -> {
                    log.error("Método de pago no configurado para provider: MERCADOPAGO, code: CARD");
                    return new EntityNotFoundException("Método de pago no configurado para provider: MERCADOPAGO");
                });

        User user = order.getCustomer().getUser();
        PaymentProfile paymentProfile = user.getPaymentProfile();
        if (paymentProfile == null) {
            log.info("Creando nuevo PaymentProfile para usuario: {}", user.getId());
            paymentProfile = PaymentProfile.builder()
                    .user(user)
                    .docType(request.getDocType())
                    .docNumber(request.getDocNumber())
                    .build();
            user.setPaymentProfile(paymentProfile);
            paymentProfileRepository.save(paymentProfile);
        }

        MercadoPagoCheckoutRequest mpRequest = mapToMercadoPagoRequest(order, request, paymentProfile);

        log.info("Enviando solicitud de pago a MercadoPago por monto: {}", mpRequest.getTransactionAmount());
        var mpPayment = mercadoPagoService.createPayment(mpRequest);
        log.info("Respuesta de MercadoPago recibida. Status: {}", mpPayment.getStatus());

        PaymentStatus status = switch (mpPayment.getStatus()) {
            case "approved" -> PaymentStatus.CONFIRMED;
            case "in_process", "pending" -> PaymentStatus.PENDING;
            case "rejected" -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(order.getTotal())
                .isOnline(true)
                .transactionCode(String.valueOf(mpPayment.getId()))
                .status(status)
                .date(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Pago guardado en DB con ID: {}", savedPayment.getId());

        if (status == PaymentStatus.CONFIRMED) {
            log.info("Pago confirmado. Actualizando estado de la orden {} a CONFIRMED", order.getId());
            OrderStatus confirmedStatus = orderStatusRepository.findByCode("CONFIRMED")
                    .orElseThrow(() -> new EntityNotFoundException("Estado CONFIRMED no encontrado"));
            order.setStatus(confirmedStatus);
            orderRepository.save(order);
        } else {
            log.warn("Pago online no aprobado (estado: {}). Moviendo orden {} a PENDING_CONFIRMATION para revisión.",
                    mpPayment.getStatus(), order.getId());
            OrderStatus pendingConfirmStatus = orderStatusRepository.findByCode("PENDING_CONFIRMATION")
                    .orElseThrow(() -> new EntityNotFoundException("Estado PENDING_CONFIRMATION no encontrado"));
            order.setStatus(pendingConfirmStatus);
            orderRepository.save(order);
        }

        return mapToResponse(savedPayment);
    }

    @Transactional
    public void createInOrder(Order order, PaymentInOrderRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Método de pago no encontrado con id: " + request.getPaymentMethodId()));

        PaymentStatus defaultStatus = request.getIsOnline()
                ? PaymentStatus.PENDING
                : PaymentStatus.CONFIRMED;

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(request.getAmount())
                .date(LocalDateTime.now())
                .isOnline(request.getIsOnline())
                .transactionCode(
                        request.getTransactionCode() != null
                                ? request.getTransactionCode()
                                : UUID.randomUUID().toString())
                .status(request.getStatus() != null ? request.getStatus() : defaultStatus)
                .build();

        order.getPayments().add(payment);
    }

    @Transactional
    public PaymentResponse update(Long id, PaymentUpdateRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

        if (request.getTransactionCode() != null) {
            payment.setTransactionCode(request.getTransactionCode());
        }
        if (request.getStatus() != null) {
            payment.setStatus(request.getStatus());
        }

        return mapToResponse(paymentRepository.save(payment));
    }

    @Transactional
    public void delete(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
    }

    private MercadoPagoCheckoutRequest mapToMercadoPagoRequest(Order order, OnlineCheckoutRequest request,
            PaymentProfile paymentProfile) {

        MercadoPagoCheckoutRequest mpRequest = new MercadoPagoCheckoutRequest();
        mpRequest.setOrderId(order.getId());

        mpRequest.setTransactionAmount(order.getTotal());

        mpRequest.setInstallments(request.getInstallments());
        mpRequest.setToken(request.getToken());
        mpRequest.setEmail(request.getEmail());
        mpRequest.setDocType(paymentProfile.getDocType());
        mpRequest.setDocNumber(paymentProfile.getDocNumber());
        mpRequest.setPaymentMethodId(request.getPaymentMethodId());

        return mpRequest;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .paymentMethodId(payment.getPaymentMethod().getId())
                .paymentMethodName(payment.getPaymentMethod().getCode())
                .amount(payment.getAmount())
                .date(payment.getDate())
                .isOnline(payment.getIsOnline())
                .transactionCode(payment.getTransactionCode())
                .provider(payment.getPaymentMethod().getProvider())
                .status(payment.getStatus())
                .build();
    }

}
