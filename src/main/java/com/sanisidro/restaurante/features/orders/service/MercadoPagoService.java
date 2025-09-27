package com.sanisidro.restaurante.features.orders.service;

import org.springframework.stereotype.Service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerAddressRequest;
import com.mercadopago.client.payment.PaymentPayerPhoneRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MercadoPagoService {

        private final PaymentClient paymentClient;

        public Payment createPayment(MercadoPagoCheckoutRequest dto) throws Exception {

                PaymentPayerRequest payer = PaymentPayerRequest.builder()
                                .email(dto.getEmail())
                                .firstName(dto.getFirstName())
                                .lastName(dto.getLastName())
                                .identification(
                                                IdentificationRequest.builder()
                                                                .type(dto.getDocType())
                                                                .number(dto.getDocNumber())
                                                                .build())
                                .address(
                                                PaymentPayerAddressRequest.builder()
                                                                .streetName(dto.getStreet())
                                                                .zipCode(dto.getZipCode())
                                                                .city(dto.getCity())
                                                                .build())
                                .phone(
                                                PaymentPayerPhoneRequest.builder()
                                                                .areaCode(dto.getAreaCode() != null ? dto.getAreaCode()
                                                                                : "51")
                                                                .number(dto.getPhone())
                                                                .build())
                                .build();

                PaymentCreateRequest request = PaymentCreateRequest.builder()
                                .transactionAmount(dto.getAmount())
                                .token(dto.getToken()) // token recibido desde frontend
                                .description("Pago de la orden #" + dto.getOrderId())
                                .installments(1)
                                .paymentMethodId(dto.getPaymentMethodId())
                                .payer(payer)
                                .build();

                return paymentClient.create(request);
        }
}