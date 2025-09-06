package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.response.PaymentResponse;
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

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;

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
    public PaymentResponse create(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId()));

        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + request.getPaymentMethodId()));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(request.getAmount())
                .isOnline(request.getIsOnline())
                .transactionCode(request.getTransactionCode())
                .date(LocalDateTime.now())
                .build();

        return mapToResponse(paymentRepository.save(payment));
    }

    @Transactional
    public void createInOrder(Order order, PaymentInOrderRequest request) {
        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + request.getPaymentMethodId()));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(method)
                .amount(request.getAmount())
                .date(LocalDateTime.now())
                .isOnline(request.getIsOnline())
                .transactionCode(request.getTransactionCode())
                .build();

        order.getPayments().add(payment);
    }

    @Transactional
    public PaymentResponse update(Long id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId()));

        PaymentMethod method = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new EntityNotFoundException("Método de pago no encontrado con id: " + request.getPaymentMethodId()));

        payment.setOrder(order);
        payment.setPaymentMethod(method);
        payment.setAmount(request.getAmount());
        payment.setIsOnline(request.getIsOnline());
        payment.setTransactionCode(request.getTransactionCode());

        return mapToResponse(paymentRepository.save(payment));
    }

    public void delete(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new EntityNotFoundException("Pago no encontrado con id: " + id);
        }
        paymentRepository.deleteById(id);
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
                .build();
    }
}
