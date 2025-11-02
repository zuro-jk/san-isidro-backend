package com.sanisidro.restaurante.features.orders.init;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderStatusTranslation;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeStatusFlow;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import com.sanisidro.restaurante.features.orders.model.PaymentMethod;
import com.sanisidro.restaurante.features.orders.model.PaymentMethodTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeStatusFlowRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodRepository;
import com.sanisidro.restaurante.features.orders.repository.PaymentMethodTranslationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(6)
public class OrderInitializer implements CommandLineRunner {

        private final OrderStatusRepository orderStatusRepository;
        private final OrderTypeRepository orderTypeRepository;
        private final OrderTypeStatusFlowRepository orderTypeStatusFlowRepository;
        private final PaymentMethodRepository paymentMethodRepository;
        private final PaymentMethodTranslationRepository paymentMethodTranslationRepository;

        @Override
        public void run(String... args) throws Exception {
                initOrderStatuses();
                initOrderTypes();
                initPaymentMethods();
                initOrderFlows();
        }

        private void initOrderStatuses() {
                if (orderStatusRepository.count() > 0) {
                        log.info(">>> Order statuses ya inicializados");
                        return;
                }

                log.info(">>> Inicializando Order Statuses...");

                OrderStatus pending = OrderStatus.builder().code("PENDING").build();
                pending.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(pending).lang("en").name("Pending")
                                                .description("Order has been placed but not processed yet").build(),
                                OrderStatusTranslation.builder().orderStatus(pending).lang("es").name("Pendiente")
                                                .description("El pedido ha sido realizado pero no procesado").build()));

                OrderStatus inProgress = OrderStatus.builder().code("IN_PROGRESS").build();
                inProgress.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(inProgress).lang("en").name("In Progress")
                                                .description("Order is being prepared").build(),
                                OrderStatusTranslation.builder().orderStatus(inProgress).lang("es").name("En Proceso")
                                                .description("El pedido está siendo preparado").build()));

                OrderStatus readyForPickup = OrderStatus.builder().code("READY_FOR_PICKUP").build();
                readyForPickup.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(readyForPickup).lang("en")
                                                .name("Ready for Pickup").description("Order is ready to be picked up")
                                                .build(),
                                OrderStatusTranslation.builder().orderStatus(readyForPickup).lang("es")
                                                .name("Listo para recoger")
                                                .description("El pedido está listo para ser recogido").build()));

                OrderStatus completed = OrderStatus.builder().code("COMPLETED").build();
                completed.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(completed).lang("en").name("Completed")
                                                .description("Order has been completed").build(),
                                OrderStatusTranslation.builder().orderStatus(completed).lang("es").name("Completado")
                                                .description("El pedido ha sido completado").build()));

                OrderStatus confirmed = OrderStatus.builder().code("CONFIRMED").build();
                confirmed.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(confirmed).lang("en").name("Confirmed")
                                                .description("Order payment has been confirmed").build(),
                                OrderStatusTranslation.builder().orderStatus(confirmed).lang("es").name("Confirmado")
                                                .description("El pago del pedido ha sido confirmado").build()));

                OrderStatus cancelled = OrderStatus.builder().code("CANCELLED").build();
                cancelled.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(cancelled).lang("en").name("Cancelled")
                                                .description("Order has been cancelled").build(),
                                OrderStatusTranslation.builder().orderStatus(cancelled).lang("es").name("Cancelado")
                                                .description("El pedido ha sido cancelado").build()));

                OrderStatus failed = OrderStatus.builder().code("FAILED").build();
                failed.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(failed).lang("en").name("Failed")
                                                .description("Order could not be processed").build(),
                                OrderStatusTranslation.builder().orderStatus(failed).lang("es").name("Fallido")
                                                .description("El pedido no pudo ser procesado").build()));
                OrderStatus outForDelivery = OrderStatus.builder().code("OUT_FOR_DELIVERY").build();
                outForDelivery.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(outForDelivery).lang("en")
                                                .name("Out for Delivery")
                                                .description("Order is on its way to the customer")
                                                .build(),
                                OrderStatusTranslation.builder().orderStatus(outForDelivery).lang("es")
                                                .name("En camino").description("El pedido está en camino al cliente")
                                                .build()));
                OrderStatus delivered = OrderStatus.builder().code("DELIVERED").build();
                delivered.getTranslations().addAll(List.of(
                                OrderStatusTranslation.builder().orderStatus(delivered).lang("en")
                                                .name("Delivered").description("Order has been delivered").build(),
                                OrderStatusTranslation.builder().orderStatus(delivered).lang("es")
                                                .name("Entregado").description("El pedido ha sido entregado").build()));

                List<OrderStatus> statuses = List.of(pending, inProgress, readyForPickup, confirmed, cancelled, failed,
                                outForDelivery, delivered, completed);
                orderStatusRepository.saveAll(statuses);

                statuses.forEach(s -> log.info(">>> OrderStatus '{}' inicializado con traducciones", s.getCode()));
        }

        private void initOrderTypes() {
                if (orderTypeRepository.count() > 0) {
                        log.info(">>> Order types ya inicializados");
                        return;
                }

                log.info(">>> Inicializando Order Types...");

                OrderType dineIn = OrderType.builder().code("DINE_IN").build();
                dineIn.getTranslations().addAll(List.of(
                                OrderTypeTranslation.builder().orderType(dineIn).lang("en").name("Dine-in")
                                                .description("Order for consumption inside the restaurant").build(),
                                OrderTypeTranslation.builder().orderType(dineIn).lang("es").name("Presencial")
                                                .description("Pedido para consumo dentro del restaurante").build()));

                OrderType takeAway = OrderType.builder().code("TAKE_AWAY").build();
                takeAway.getTranslations().addAll(List.of(
                                OrderTypeTranslation.builder().orderType(takeAway).lang("en").name("Take Away")
                                                .description("Order to be picked up and taken away").build(),
                                OrderTypeTranslation.builder().orderType(takeAway).lang("es").name("Para llevar")
                                                .description("Pedido para recoger y llevar").build()));

                OrderType delivery = OrderType.builder().code("DELIVERY").build();
                delivery.getTranslations().addAll(List.of(
                                OrderTypeTranslation.builder().orderType(delivery).lang("en").name("Delivery")
                                                .description("Order to be delivered to customer's address").build(),
                                OrderTypeTranslation.builder().orderType(delivery).lang("es")
                                                .name("Entrega a domicilio")
                                                .description("Pedido a ser entregado en la dirección del cliente")
                                                .build()));

                List<OrderType> types = List.of(dineIn, takeAway, delivery);
                orderTypeRepository.saveAll(types);

                types.forEach(t -> log.info(">>> OrderType '{}' inicializado con traducciones", t.getCode()));
        }

        private void initPaymentMethods() {
                if (paymentMethodRepository.count() > 0) {
                        log.info(">>> Métodos de pago ya inicializados");
                        return;
                }

                log.info(">>> Inicializando métodos de pago...");

                List<PaymentMethod> methods = List.of(
                                PaymentMethod.builder().code("CASH").provider("INTERNAL").build(),
                                PaymentMethod.builder().code("CARD").provider("MERCADOPAGO").build(),
                                PaymentMethod.builder().code("YAPE").provider("INTERNAL").build(),
                                PaymentMethod.builder().code("PLIN").provider("INTERNAL").build(),
                                PaymentMethod.builder().code("TRANSFER").provider("INTERNAL").build());

                paymentMethodRepository.saveAll(methods);

                List<PaymentMethodTranslation> translations = new ArrayList<>();

                for (PaymentMethod method : methods) {
                        switch (method.getCode()) {
                                case "CASH" -> {
                                        translations.addAll(List.of(
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("es").name("Efectivo")
                                                                        .description("Pago en efectivo en el restaurante")
                                                                        .build(),
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("en").name("Cash")
                                                                        .description("Cash payment at the restaurant")
                                                                        .build()));
                                }
                                case "CARD" -> {
                                        translations.addAll(List.of(
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("es").name("Tarjeta")
                                                                        .description("Pago con tarjeta de crédito o débito vía MercadoPago")
                                                                        .build(),
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("en").name("Card")
                                                                        .description("Credit or debit card payment via MercadoPago")
                                                                        .build()));
                                }
                                case "YAPE" -> {
                                        translations.addAll(List.of(
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("es").name("Yape")
                                                                        .description("Pago mediante Yape")
                                                                        .build(),
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("en").name("Yape")
                                                                        .description("Payment via Yape")
                                                                        .build()));
                                }
                                case "PLIN" -> {
                                        translations.addAll(List.of(
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("es").name("Plin")
                                                                        .description("Pago mediante Plin")
                                                                        .build(),
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("en").name("Plin")
                                                                        .description("Payment via Plin")
                                                                        .build()));
                                }
                                case "TRANSFER" -> {
                                        translations.addAll(List.of(
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("es").name("Transferencia Bancaria")
                                                                        .description("Pago mediante transferencia bancaria")
                                                                        .build(),
                                                        PaymentMethodTranslation.builder()
                                                                        .paymentMethod(method)
                                                                        .lang("en").name("Bank Transfer")
                                                                        .description("Payment via bank transfer")
                                                                        .build()));
                                }
                        }
                }

                paymentMethodTranslationRepository.saveAll(translations);

                log.info(">>> Métodos de pago inicializados correctamente");
        }

        private void initOrderFlows() {
                if (orderTypeStatusFlowRepository.count() > 0) {
                        log.info(">>> Flujos de Estado de Órdenes ya inicializados");
                        return;
                }

                log.info(">>> Inicializando Flujos de Estado de Órdenes...");

                // --- Cargar Tipos de Orden ---
                OrderType deliveryType = orderTypeRepository.findByCode("DELIVERY").orElseThrow();
                OrderType dineInType = orderTypeRepository.findByCode("DINE_IN").orElseThrow();
                OrderType takeAwayType = orderTypeRepository.findByCode("TAKE_AWAY").orElseThrow();

                // --- Cargar Estados ---
                OrderStatus pending = orderStatusRepository.findByCode("PENDING").orElseThrow();
                OrderStatus confirmed = orderStatusRepository.findByCode("CONFIRMED").orElseThrow();
                OrderStatus inProgress = orderStatusRepository.findByCode("IN_PROGRESS").orElseThrow();
                OrderStatus outForDelivery = orderStatusRepository.findByCode("OUT_FOR_DELIVERY").orElseThrow();
                OrderStatus delivered = orderStatusRepository.findByCode("DELIVERED").orElseThrow();
                OrderStatus readyForPickup = orderStatusRepository.findByCode("READY_FOR_PICKUP").orElseThrow();
                OrderStatus completed = orderStatusRepository.findByCode("COMPLETED").orElseThrow();

                // --- 1. Flujo para "DELIVERY" (Entrega a domicilio) ---
                List<OrderTypeStatusFlow> deliveryFlow = List.of(
                                new OrderTypeStatusFlow(null, deliveryType, pending, 0),
                                new OrderTypeStatusFlow(null, deliveryType, confirmed, 1),
                                new OrderTypeStatusFlow(null, deliveryType, inProgress, 2),
                                new OrderTypeStatusFlow(null, deliveryType, outForDelivery, 3),
                                new OrderTypeStatusFlow(null, deliveryType, delivered, 4));
                orderTypeStatusFlowRepository.saveAll(deliveryFlow);

                // --- 2. Flujo para "TAKE_AWAY" (Para llevar) ---
                List<OrderTypeStatusFlow> takeAwayFlow = List.of(
                                new OrderTypeStatusFlow(null, takeAwayType, pending, 0),
                                new OrderTypeStatusFlow(null, takeAwayType, confirmed, 1),
                                new OrderTypeStatusFlow(null, takeAwayType, inProgress, 2),
                                new OrderTypeStatusFlow(null, takeAwayType, readyForPickup, 3),
                                new OrderTypeStatusFlow(null, takeAwayType, completed, 4));
                orderTypeStatusFlowRepository.saveAll(takeAwayFlow);

                // --- 3. Flujo para "DINE_IN" (En restaurante) ---
                List<OrderTypeStatusFlow> dineInFlow = List.of(
                                new OrderTypeStatusFlow(null, dineInType, pending, 0),
                                new OrderTypeStatusFlow(null, dineInType, confirmed, 1),
                                new OrderTypeStatusFlow(null, dineInType, inProgress, 2),
                                new OrderTypeStatusFlow(null, dineInType, completed, 3));
                orderTypeStatusFlowRepository.saveAll(dineInFlow);

                log.info(">>> Flujos de Estado inicializados.");
        }

}
