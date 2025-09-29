package com.sanisidro.restaurante.features.orders.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sanisidro.restaurante.core.security.model.PaymentProfile;
import com.sanisidro.restaurante.core.security.repository.PaymentProfileRepository;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.OnlineCheckoutRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentUpdateRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.response.PaymentResponse;
import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.Payment;
import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
    public PaymentResponse createOnlinePayment(OnlineCheckoutRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden no encontrada con id: " + request.getOrderId()));

        PaymentMethod method = paymentMethodRepository.findByCodeAndProvider("CARD", "MERCADOPAGO")
                .orElseThrow(() -> new EntityNotFoundException(
                        "Método de pago no configurado para provider: MERCADOPAGO"));

        var user = order.getCustomer().getUser();
        PaymentProfile paymentProfile = user.getPaymentProfile();
        if (paymentProfile == null) {
            paymentProfile = PaymentProfile.builder()
                    .user(user)
                    .docType(request.getDocType())
                    .docNumber(request.getDocNumber())
                    .build();
            user.setPaymentProfile(paymentProfile);
            paymentProfileRepository.save(paymentProfile);
        }

        try {
            MercadoPagoCheckoutRequest mpRequest = mapToMercadoPagoRequest(order, request, paymentProfile);

            var mpPayment = mercadoPagoService.createPayment(mpRequest);

            PaymentStatus status = switch (mpPayment.getStatus()) {
                case "approved" -> PaymentStatus.CONFIRMED;
                case "in_process", "pending" -> PaymentStatus.PENDING;
                case "rejected" -> PaymentStatus.FAILED;
                default -> PaymentStatus.PENDING;
            };

            // Guardar el pago en DB
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

            // Actualizar estado de la orden si el pago fue confirmado
            if (status == PaymentStatus.CONFIRMED) {
                OrderStatus confirmedStatus = orderStatusRepository.findByCode("CONFIRMED")
                        .orElseThrow(() -> new EntityNotFoundException("Estado CONFIRMED no encontrado"));
                order.setStatus(confirmedStatus);
                orderRepository.save(order);
            }

            return mapToResponse(savedPayment);

        } catch (Exception e) {
            throw new RuntimeException("Error creando pago online con MercadoPago", e);
        }
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

    private MercadoPagoCheckoutRequest mapToMercadoPagoRequest(Order order, OnlineCheckoutRequest request, PaymentProfile paymentProfile) {
        MercadoPagoCheckoutRequest mpRequest = new MercadoPagoCheckoutRequest();
        mpRequest.setOrderId(order.getId());
        mpRequest.setTransactionAmount(request.getTransactionAmount());
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
