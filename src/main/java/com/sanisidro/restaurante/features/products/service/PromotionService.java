package com.sanisidro.restaurante.features.products.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.products.dto.promotion.request.CreatePromotionRequest;
import com.sanisidro.restaurante.features.products.dto.promotion.request.UpdatePromotionRequest;
import com.sanisidro.restaurante.features.products.dto.promotion.response.PromotionResponse;
import com.sanisidro.restaurante.features.products.model.Category;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.model.Promotion;
import com.sanisidro.restaurante.features.products.repository.CategoryRepository;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import com.sanisidro.restaurante.features.products.repository.PromotionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<PromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = findByIdOrThrow(id);
        return mapToResponse(promotion);
    }

    @Transactional
    public PromotionResponse createPromotion(CreatePromotionRequest request) {

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Set<Product> products = new HashSet<>(productRepository.findAllById(request.getApplicableProductIds()));
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getApplicableCategoryIds()));

        Promotion newPromotion = Promotion.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .applicableProducts(products)
                .applicableCategories(categories)
                .build();

        Promotion savedPromotion = promotionRepository.save(newPromotion);
        return mapToResponse(savedPromotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Long id, UpdatePromotionRequest request) {
        Promotion promotion = findByIdOrThrow(id);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        Set<Product> products = new HashSet<>(productRepository.findAllById(request.getApplicableProductIds()));
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getApplicableCategoryIds()));

        promotion.setName(request.getName());
        promotion.setDescription(request.getDescription());
        promotion.setStartDate(request.getStartDate());
        promotion.setEndDate(request.getEndDate());
        promotion.setActive(request.getActive());
        promotion.setDiscountType(request.getDiscountType());
        promotion.setDiscountValue(request.getDiscountValue());
        promotion.setApplicableProducts(products);
        promotion.setApplicableCategories(categories);

        Promotion updatedPromotion = promotionRepository.save(promotion);
        return mapToResponse(updatedPromotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        if (!promotionRepository.existsById(id)) {
            throw new EntityNotFoundException("Promoción no encontrada con id: " + id);
        }
        promotionRepository.deleteById(id);
    }

    private Promotion findByIdOrThrow(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada con id: " + id));
    }

    private PromotionResponse mapToResponse(Promotion promotion) {

        Set<PromotionResponse.ProductStub> productStubs = promotion.getApplicableProducts().stream()
                .map(p -> new PromotionResponse.ProductStub(p.getId(), p.getName()))
                .collect(Collectors.toSet());

        Set<PromotionResponse.CategoryStub> categoryStubs = promotion.getApplicableCategories().stream()
                .map(c -> new PromotionResponse.CategoryStub(c.getId(), c.getName()))
                .collect(Collectors.toSet());

        return PromotionResponse.builder()
                .id(promotion.getId())
                .name(promotion.getName())
                .description(promotion.getDescription())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .active(promotion.isActive())
                .discountType(promotion.getDiscountType())
                .discountValue(promotion.getDiscountValue())
                .applicableProducts(productStubs)
                .applicableCategories(categoryStubs)
                .build();
    }

}
