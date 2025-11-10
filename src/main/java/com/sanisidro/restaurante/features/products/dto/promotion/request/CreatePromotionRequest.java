package com.sanisidro.restaurante.features.products.dto.promotion.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.sanisidro.restaurante.features.products.enums.DiscountType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePromotionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio no puede ser en el pasado")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "La fecha de fin debe ser en el futuro")
    private LocalDateTime endDate;

    @NotNull(message = "El estado (activo/inactivo) es obligatorio")
    private Boolean active;

    @NotNull(message = "El tipo de descuento es obligatorio")
    private DiscountType discountType;

    @NotNull(message = "El valor del descuento es obligatorio")
    @Positive(message = "El valor del descuento debe ser positivo")
    private BigDecimal discountValue;

    private Set<Long> applicableProductIds;
    private Set<Long> applicableCategoryIds;
}