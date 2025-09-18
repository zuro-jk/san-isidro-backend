package com.sanisidro.restaurante.features.employees.dto.employee.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PositionResponse {
    private Long id;
    private String name;
    private String description;

    private Set<RoleSummary> roles;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleSummary {
        private Long id;
        private String name;
    }
}
