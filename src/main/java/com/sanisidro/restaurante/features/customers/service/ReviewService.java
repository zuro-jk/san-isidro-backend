package com.sanisidro.restaurante.features.customers.service;

import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.customers.dto.review.request.ReviewRequest;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.Review;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.ReviewRepository;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;


    public ReviewResponse getReview(Long id) {
        return mapToResponse(reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada")));
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

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        Review review = Review.builder()
                .customer(customer)
                .comment(dto.getComment())
                .rating(dto.getRating())
                .date(dto.getDate())
                .build();

        return mapToResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(Long id, ReviewRequest dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        if (dto.getComment() != null) review.setComment(dto.getComment());
        if (dto.getRating() != null) review.setRating(dto.getRating());
        if (dto.getDate() != null) review.setDate(dto.getDate());

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
                .comment(review.getComment())
                .rating(review.getRating())
                .date(review.getDate())
                .build();
    }
}
