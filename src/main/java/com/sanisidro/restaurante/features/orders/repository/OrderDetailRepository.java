package com.sanisidro.restaurante.features.orders.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sanisidro.restaurante.features.orders.model.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query("""
                SELECT d.product.id, d.product.name, SUM(d.quantity), SUM(d.quantity * d.unitPrice)
                FROM OrderDetail d
                GROUP BY d.product.id, d.product.name
                ORDER BY SUM(d.quantity) DESC
            """)
    List<Object[]> findTopSellingProducts();

}
