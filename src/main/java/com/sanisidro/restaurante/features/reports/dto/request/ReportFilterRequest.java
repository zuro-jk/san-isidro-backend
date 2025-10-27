package com.sanisidro.restaurante.features.reports.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ReportFilterRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}