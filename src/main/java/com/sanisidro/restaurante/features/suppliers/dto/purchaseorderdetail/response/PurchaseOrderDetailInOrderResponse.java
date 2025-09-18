package com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PurchaseOrderDetailInOrderResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private Integer quantity;
    private BigDecimal unitPrice;
}