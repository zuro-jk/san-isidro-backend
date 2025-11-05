package com.sanisidro.restaurante.features.feedbackloyalty.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardResponse {
    private Long id;
    private String name;
    private String description;
    private int requiredPoints;
}