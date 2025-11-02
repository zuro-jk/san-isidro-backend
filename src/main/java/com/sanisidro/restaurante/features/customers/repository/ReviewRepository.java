package com.sanisidro.restaurante.features.customers.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sanisidro.restaurante.features.customers.model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Obtener reviews por cliente
    List<Review> findByCustomerId(Long customerId);

    Optional<Review> findByIdAndCustomer_Id(Long reviewId, Long customerId);

    boolean existsByIdAndCustomer_Id(Long reviewId, Long customerId);
    
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

    @Query("SELECT r FROM Review r ORDER BY r.date DESC LIMIT :limit")
    List<Review> findRecentReviews(@Param("limit") int limit);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r")
    int calculateAverageSatisfaction();

}
