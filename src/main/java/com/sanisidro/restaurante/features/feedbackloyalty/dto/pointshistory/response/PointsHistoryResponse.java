package com.sanisidro.restaurante.features.feedbackloyalty.dto.pointshistory.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PointsHistoryResponse {
    private int points;
    private String event;
    private LocalDateTime createdAt;
}