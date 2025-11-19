package com.sanisidro.restaurante.features.customers.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.core.security.model.User;
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

    public List<ReviewResponse> getMeReviews(User user) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return this.reviewRepository.findByCustomerId(customer.getId()).stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public boolean hasCustomerReviewed(User user, Long orderId, Long productId, Long reservationId) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        boolean exists = false;

        if (orderId != null) {
            exists = reviewRepository.existsByCustomer_IdAndOrder_Id(customer.getId(), orderId);
        } else if (productId != null) {
            exists = reviewRepository.existsByCustomer_IdAndProduct_Id(customer.getId(), productId);
        } else if (reservationId != null) {
            exists = reviewRepository.existsByCustomer_IdAndReservation_Id(customer.getId(), reservationId);
        } else {
            throw new IllegalArgumentException("Debe especificar orderId, productId o reservationId");
        }

        log.debug("Verificación de reseña: customerId={}, exists={}, orderId={}, productId={}, reservationId={}",
                customer.getId(), exists, orderId, productId, reservationId);

        return exists;
    }

    public ReviewResponse createReview(ReviewRequest dto, User user) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para el usuario autenticado"));

        int count = 0;
        if (dto.getOrderId() != null)
            count++;
        if (dto.getReservationId() != null)
            count++;
        if (dto.getProductId() != null)
            count++;

        if (count != 1) {
            throw new IllegalArgumentException("Debe especificar exactamente uno: orderId, reservationId o productId");
        }

        if (dto.getOrderId() != null) {
            if (!orderRepository.existsByIdAndCustomer_Id(dto.getOrderId(), customer.getId())) {
                throw new AccessDeniedException("No puede reseñar una orden que no le pertenece.");
            }
            if (reviewRepository.existsByCustomer_IdAndOrder_Id(customer.getId(), dto.getOrderId())) {
                throw new IllegalArgumentException("Ya has enviado una reseña para esta orden.");
            }
        }

        if (dto.getReservationId() != null) {
            if (!reservationRepository.existsByIdAndCustomer_Id(dto.getReservationId(), customer.getId())) {
                throw new AccessDeniedException("No puede reseñar una reserva que no le pertenece.");
            }
            if (reviewRepository.existsByCustomer_IdAndReservation_Id(customer.getId(), dto.getReservationId())) {
                throw new IllegalArgumentException("Ya has enviado una reseña para esta reserva.");
            }
        }

        if (dto.getProductId() != null) {
            if (reviewRepository.existsByCustomer_IdAndProduct_Id(customer.getId(), dto.getProductId())) {
                throw new IllegalArgumentException("Ya has enviado una reseña para este producto.");
            }
        }

        Review review = Review.builder()
                .customer(customer)
                .comment(dto.getComment())
                .rating(dto.getRating())
                .date(LocalDateTime.now())
                .build();

        if (dto.getOrderId() != null) {

            Order order = orderRepository.findById(dto.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

            review.setOrder(order);

        } else if (dto.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(dto.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

            review.setReservation(reservation);

        } else if (dto.getProductId() != null) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
            review.setProduct(product);
        }

        Review saved = reviewRepository.save(review);
        log.info("Review creada: id={}, customerId={}, recurso={}", saved.getId(), customer.getId(),
                getResourceType(saved));

        return mapToResponse(saved);
    }

    public ReviewResponse updateReview(Long id, ReviewRequest dto, User user) {

        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        Review review = reviewRepository.findByIdAndCustomer_Id(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada o no pertenece al usuario"));

        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }
        if (dto.getRating() != null) {
            review.setRating(dto.getRating());
        }

        Review updated = reviewRepository.save(review);
        log.info("Review actualizada: id={}, customerId={}", updated.getId(), customer.getId());
        return mapToResponse(updated);
    }

    public void deleteReview(Long id, User user) {
        Customer customer = customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (!reviewRepository.existsByIdAndCustomer_Id(id, customer.getId())) {
            throw new ResourceNotFoundException("Reseña no encontrada o no pertenece al usuario");
        }

        reviewRepository.deleteById(id);
        log.info("Review eliminada: id={}, customerId={}", id, customer.getId());
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
        if (review.getOrder() != null)
            return "Order";
        if (review.getReservation() != null)
            return "Reservation";
        if (review.getProduct() != null)
            return "Product";
        return "Unknown";
    }

    public List<ReviewResponse> findRecentReviews(int limit) {
        return reviewRepository.findRecentReviews(limit)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public int calculateAverageSatisfaction() {
        return reviewRepository.calculateAverageSatisfaction();
    }
}
