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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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
            review.setOrder(order);
        }

        if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
            review.setReservation(reservation);
        }

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            review.setProduct(product);
        }

        Review saved = reviewRepository.save(review);
        log.info("Review creada: id={}, customerId={}, recurso={}", saved.getId(), customer.getId(),
                getResourceType(saved));
        return mapToResponse(saved);
    }

    public ReviewResponse updateReview(Long id, ReviewRequest dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        if (dto.getComment() != null) review.setComment(dto.getComment());
        if (dto.getRating() != null) review.setRating(dto.getRating());

        if (dto.getOrderId() != null) {
            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
            review.setOrder(order);
            review.setReservation(null);
            review.setProduct(null);
        }

        if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
            review.setReservation(reservation);
            review.setOrder(null);
            review.setProduct(null);
        }

        if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            review.setProduct(product);
            review.setOrder(null);
            review.setReservation(null);
        }

        Review updated = reviewRepository.save(review);
        log.info("Review actualizada: id={}, customerId={}, recurso={}", updated.getId(),
                updated.getCustomer().getId(), getResourceType(updated));
        return mapToResponse(updated);
    }

    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reseña no encontrada");
        }
        reviewRepository.deleteById(id);
        log.info("Review eliminada: id={}", id);
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

    private String getResourceType(Review review) {
        if (review.getOrder() != null) return "Order";
        if (review.getReservation() != null) return "Reservation";
        if (review.getProduct() != null) return "Product";
        return "Unknown";
    }
}
