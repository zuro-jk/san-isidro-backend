package com.sanisidro.restaurante.features.orders.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.orders.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);

    @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.date BETWEEN :startOfDay AND :endOfDay")
    int countOrdersByDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
            SELECT COALESCE(SUM(o.total), 0)
            FROM CustomerOrder o
            WHERE o.date BETWEEN :startOfDay AND :endOfDay
            """)
    BigDecimal calculateSalesByDate(
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
                SELECT COALESCE(SUM(o.total), 0)
                FROM CustomerOrder o
                WHERE o.status.code IN :validStatuses
            """)
    BigDecimal sumTotalByStatusCodes(@Param("validStatuses") List<String> validStatuses);

    @Query("""
                SELECT tr.name, COUNT(o), COALESCE(SUM(o.total), 0)
                FROM CustomerOrder o
                JOIN o.type ot
                JOIN ot.translations tr
                WHERE tr.lang = :lang
                GROUP BY tr.name
            """)
    List<Object[]> findOrderTypeStatistics(@Param("lang") String lang);
}
