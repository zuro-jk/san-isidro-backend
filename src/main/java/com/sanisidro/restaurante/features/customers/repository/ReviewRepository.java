package com.sanisidro.restaurante.features.customers.repository;

import com.sanisidro.restaurante.features.customers.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Obtener reviews por cliente
    List<Review> findByCustomerId(Long customerId);

    // Validaciones para review por Orden
    boolean existsByCustomer_IdAndOrder_Id(Long customerId, Long orderId);
    boolean existsByCustomer_IdAndOrder_IdAndIdNot(Long customerId, Long orderId, Long id);

    // Validaciones para review por Reserva
    boolean existsByCustomer_IdAndReservation_Id(Long customerId, Long reservationId);
    boolean existsByCustomer_IdAndReservation_IdAndIdNot(Long customerId, Long reservationId, Long id);

    // Validaciones para review por Producto
    boolean existsByCustomer_IdAndProduct_Id(Long customerId, Long productId);
    boolean existsByCustomer_IdAndProduct_IdAndIdNot(Long customerId, Long productId, Long id);

    // (Opcional) Obtener reviews por orden, reserva o producto
    List<Review> findByOrder_Id(Long orderId);
    List<Review> findByReservation_Id(Long reservationId);
    List<Review> findByProduct_Id(Long productId);

}
