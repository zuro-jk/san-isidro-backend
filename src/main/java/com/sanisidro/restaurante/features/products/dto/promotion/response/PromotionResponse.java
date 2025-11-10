package com.sanisidro.restaurante.features.products.dto.promotion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.sanisidro.restaurante.features.products.enums.DiscountType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private DiscountType discountType;
    private BigDecimal discountValue;

    private Set<ProductStub> applicableProducts;
    private Set<CategoryStub> applicableCategories;

    @Data
    @AllArgsConstructor
    public static class ProductStub {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class CategoryStub {
        private Long id;
        private String name;
    }
}