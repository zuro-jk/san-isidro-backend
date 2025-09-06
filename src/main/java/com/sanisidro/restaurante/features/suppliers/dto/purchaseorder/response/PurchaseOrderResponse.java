package com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.response;

import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private PurchaseOrderStatus status;
    private BigDecimal total;
    private LocalDateTime date;
    private List<PurchaseOrderDetailInOrderResponse> details;
}
