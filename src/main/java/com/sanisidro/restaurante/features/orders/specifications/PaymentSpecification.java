package com.sanisidro.restaurante.features.orders.specifications;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.model.Payment;

import jakarta.persistence.criteria.Join;

public class PaymentSpecification {

    /**
     * Combina todas las especificaciones.
     * Si un filtro es nulo, simplemente se ignora.
     */
    public static Specification<Payment> withFilters(
            Long customerId,
            Long paymentMethodId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo) {

        return Specification
                .where(hasCustomerId(customerId))
                .and(hasPaymentMethodId(paymentMethodId))
                .and(isDateBetween(dateFrom, dateTo));
    }

    /**
     * Filtra por ID de Cliente.
     * Necesita hacer un "JOIN" a través de la Orden.
     */
    private static Specification<Payment> hasCustomerId(Long customerId) {
        return (root, query, cb) -> {
            if (customerId == null) {
                return cb.conjunction();
            }
            Join<Payment, Order> orderJoin = root.join("order");
            Join<Order, Customer> customerJoin = orderJoin.join("customer");
            return cb.equal(customerJoin.get("id"), customerId);
        };
    }

    /**
     * Filtra por ID de Método de Pago.
     */
    private static Specification<Payment> hasPaymentMethodId(Long paymentMethodId) {
        return (root, query, cb) -> {
            if (paymentMethodId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("paymentMethod").get("id"), paymentMethodId);
        };
    }

    /**
     * Filtra por un rango de fechas.
     */
    private static Specification<Payment> isDateBetween(LocalDateTime dateFrom, LocalDateTime dateTo) {
        return (root, query, cb) -> {
            if (dateFrom != null && dateTo != null) {
                LocalDateTime endOfDay = dateTo.toLocalDate().atTime(23, 59, 59);
                return cb.between(root.get("date"), dateFrom, endOfDay);
            }
            if (dateFrom != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), dateFrom);
            }
            if (dateTo != null) {
                LocalDateTime endOfDay = dateTo.toLocalDate().atTime(23, 59, 59);
                return cb.lessThanOrEqualTo(root.get("date"), endOfDay);
            }
            return cb.conjunction();
        };
    }

}
