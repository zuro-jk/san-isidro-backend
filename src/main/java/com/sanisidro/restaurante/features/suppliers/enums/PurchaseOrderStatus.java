package com.sanisidro.restaurante.features.suppliers.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PurchaseOrderStatus {
    PENDING,
    APPROVED,
    REJECTED,
    RECEIVED,
    CANCELLED;

}
