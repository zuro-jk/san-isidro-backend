package com.sanisidro.restaurante.features.orders.service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.*;
import com.mercadopago.resources.payment.Payment;
import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
                                .build()
                )
                .address(
                        PaymentPayerAddressRequest.builder()
                                .streetName(dto.getStreet())
                                .zipCode(dto.getZipCode())
                                .city(dto.getCity())
                                .build()
                )
                .phone(
                        PaymentPayerPhoneRequest.builder()
                                .areaCode(dto.getAreaCode() != null ? dto.getAreaCode() : "51")
                                .number(dto.getPhone())
                                .build()
                )
                .build();

        PaymentCreateRequest request = PaymentCreateRequest.builder()
                .transactionAmount(dto.getAmount())
                .token(dto.getToken())
                .description("Pago de la orden #" + dto.getOrderId())
                .installments(1)
                .paymentMethodId("visa")
                .payer(payer)
                .build();

        return paymentClient.create(request);
    }
}