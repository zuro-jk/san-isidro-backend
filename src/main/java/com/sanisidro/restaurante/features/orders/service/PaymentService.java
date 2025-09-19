package com.sanisidro.restaurante.features.orders.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final MercadoPagoService mercadoPagoService;

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
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId()));

        PaymentMethod method = paymentMethodRepository.findByCodeAndProvider("CARD", request.getProvider())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no configurado para provider: " + request.getProvider()));

        try {
            PaymentStatus status;
            String transactionId;

            switch (method.getProvider()) {
                case "MERCADOPAGO" -> {
                    MercadoPagoCheckoutRequest mpRequest = mapToMercadoPagoRequest(request);

                    var mpPayment = mercadoPagoService.createPayment(mpRequest);

                    status = switch (mpPayment.getStatus()) {
                        case "approved" -> PaymentStatus.CONFIRMED;
                        case "in_process", "pending" -> PaymentStatus.PENDING;
                        case "rejected" -> PaymentStatus.FAILED;
                        default -> PaymentStatus.PENDING;
                    };
                    transactionId = String.valueOf(mpPayment.getId());
                }
                default -> throw new UnsupportedOperationException(
                        "Provider no soportado: " + method.getProvider()
                );
            }

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentMethod(method)
                    .amount(request.getAmount())
                    .isOnline(true)
                    .transactionCode(transactionId)
                    .status(status)
                    .date(LocalDateTime.now())
                    .build();

            return mapToResponse(paymentRepository.save(payment));

        } catch (Exception e) {
            throw new RuntimeException("Error creando pago online con provider " + method.getProvider(), e);
        }
    }


    @Transactional
    public void createInOrder(Order order, PaymentInOrderRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + request.getPaymentMethodId()));

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
                                : UUID.randomUUID().toString()
                )
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

    private MercadoPagoCheckoutRequest mapToMercadoPagoRequest(OnlineCheckoutRequest request) {
        MercadoPagoCheckoutRequest mpRequest = new MercadoPagoCheckoutRequest();
        mpRequest.setOrderId(request.getOrderId());
        mpRequest.setAmount(request.getAmount());
        mpRequest.setToken(request.getToken());
        mpRequest.setEmail(request.getEmail());
        mpRequest.setFirstName(request.getFirstName());
        mpRequest.setLastName(request.getLastName());
        mpRequest.setDocType(request.getDocType());
        mpRequest.setDocNumber(request.getDocNumber());
        mpRequest.setPhone(request.getPhone());
        mpRequest.setAreaCode(request.getAreaCode());
        mpRequest.setStreet(request.getStreet());
        mpRequest.setCity(request.getCity());
        mpRequest.setZipCode(request.getZipCode());
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
