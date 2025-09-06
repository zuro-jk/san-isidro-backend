package com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.request;

import java.math.BigDecimal;

import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.request.PurchaseOrderDetailCreateRequest;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseOrderRequest {
    @NotNull(message = "El proveedor es obligatorio")
    private Long supplierId;

    private String status;

    @NotNull(message = "Debe incluir al menos un detalle")
    @Valid
    private List<PurchaseOrderDetailCreateRequest> details;
}
