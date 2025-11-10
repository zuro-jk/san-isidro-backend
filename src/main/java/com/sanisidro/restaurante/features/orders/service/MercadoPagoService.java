package com.sanisidro.restaurante.features.orders.service;

import org.springframework.stereotype.Service;

import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.sanisidro.restaurante.features.orders.dto.payment.request.MercadoPagoCheckoutRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

        private final PaymentClient paymentClient;

        public Payment createPayment(MercadoPagoCheckoutRequest dto) throws Exception {

                PaymentPayerRequest payer = PaymentPayerRequest.builder()
                                .email(dto.getEmail())
                                .identification(
                                                IdentificationRequest.builder()
                                                                .type(dto.getDocType())
                                                                .number(dto.getDocNumber())
                                                                .build())
                                .build();

                PaymentCreateRequest request = PaymentCreateRequest.builder()
                                .transactionAmount(dto.getTransactionAmount())
                                .token(dto.getToken()) // token recibido desde frontend
                                .description("Pago de la orden #" + dto.getOrderId())
                                .installments(dto.getInstallments() != null ? dto.getInstallments() : 1)
                                .paymentMethodId(dto.getPaymentMethodId())
                                .payer(payer)
                                .build();

                try {
                        log.info("Enviando petición de pago a MercadoPago...");
                        // Opcional: loguear el request (¡cuidado con datos sensibles en producción!)
                        log.debug("PaymentCreateRequest (sin token): transactionAmount={}, paymentMethodId={}, payerEmail={}",
                                        request.getTransactionAmount(), request.getPaymentMethodId(),
                                        request.getPayer().getEmail());

                        return paymentClient.create(request);

                } catch (MPApiException apiException) {
                        // ¡¡ESTA ES LA PARTE IMPORTANTE!!
                        // Aquí capturamos la respuesta de error de la API
                        log.error("¡Error de API de MercadoPago! La solicitud fue rechazada.");
                        log.error("Status Code: {}", apiException.getStatusCode());

                        // Imprime el JSON de error de MercadoPago
                        if (apiException.getApiResponse() != null) {
                                log.error("Error Response Body: {}", apiException.getApiResponse().getContent());
                        } else {
                                log.error("Error (sin body): {}", apiException.getMessage());
                        }

                        // Re-lanzamos la excepción para que el GlobalExceptionHandler la capture
                        throw apiException;

                } catch (MPException mpException) {
                        // Otro error del SDK (ej. de red o configuración)
                        log.error("Error del SDK de MercadoPago: {}", mpException.getMessage(), mpException);
                        throw mpException;
                }
        }
}