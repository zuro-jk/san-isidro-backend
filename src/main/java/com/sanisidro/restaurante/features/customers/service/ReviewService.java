package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.model.Review;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import com.sanisidro.restaurante.features.customers.repository.ReviewRepository;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final ReservationRepository reservationRepository;
    private final ProductRepository productRepository;


    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));
        return mapToResponse(review);
    }

    public List<ReviewResponse> getReviewsByCustomer(Long customerId) {
        return reviewRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReviewResponse createReview(ReviewRequest dto) {

        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Review review = Review.builder()
                .customer(customer)
                .comment(dto.getComment())
                .rating(dto.getRating())
                .date(LocalDateTime.now())
                .build();

        int count = 0;
        if (dto.getOrderId() != null) count++;
        if (dto.getReservationId() != null) count++;
        if (dto.getProductId() != null) count++;

        if (count != 1) {
            throw new IllegalArgumentException("Debe especificar exactamente uno: orderId, reservationId o productId");
        }

        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

            if (reviewRepository.existsByCustomer_IdAndOrder_Id(customer.getId(), order.getId())) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para esta orden");
            }
            review.setOrder(order);
        }

        if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

            if (reviewRepository.existsByCustomer_IdAndReservation_Id(customer.getId(), reservation.getId())) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para esta reserva");
            }
            review.setReservation(reservation);
        }

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (reviewRepository.existsByCustomer_IdAndProduct_Id(customer.getId(), product.getId())) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para este producto");
            }
            review.setProduct(product);
        }

        return mapToResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long id, ReviewRequest dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        if (dto.getComment() != null) review.setComment(dto.getComment());
        if (dto.getRating() != null) review.setRating(dto.getRating());

        // Solo permitir cambiar el recurso si no hay otra review para ese recurso
        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
            if (reviewRepository.existsByCustomer_IdAndOrder_IdAndIdNot(review.getCustomer().getId(), order.getId(), id)) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para esta orden");
            }
            review.setOrder(order);
            review.setReservation(null);
            review.setProduct(null);
        }

        if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
            if (reviewRepository.existsByCustomer_IdAndReservation_IdAndIdNot(review.getCustomer().getId(), reservation.getId(), id)) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para esta reserva");
            }
            review.setReservation(reservation);
            review.setOrder(null);
            review.setProduct(null);
        }

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            if (reviewRepository.existsByCustomer_IdAndProduct_IdAndIdNot(review.getCustomer().getId(), product.getId(), id)) {
                throw new IllegalArgumentException("El cliente ya realizó una reseña para este producto");
            }
            review.setProduct(product);
            review.setOrder(null);
            review.setReservation(null);
        }

        return mapToResponse(reviewRepository.save(review));
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reseña no encontrada");
        }
        reviewRepository.deleteById(id);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getUser().getFullName())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .reservationId(review.getReservation() != null ? review.getReservation().getId() : null)
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .comment(review.getComment())
                .rating(review.getRating())
                .date(review.getDate())
                .build();
    }
}
