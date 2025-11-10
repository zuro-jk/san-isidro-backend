package com.sanisidro.restaurante.features.cashclosing.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CashClosingSubmitRequest {

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal countedCashAmount; // El dinero que el cajero contó

    private String notes; // Notas del cajero (ej. "Sobrante por propina")
}