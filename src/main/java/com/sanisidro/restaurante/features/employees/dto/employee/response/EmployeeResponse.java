package com.sanisidro.restaurante.features.employees.dto.employee.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {
  private Long id;
  private Long userId;

  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String fullName;
  private String profileImageUrl;
  private String phone;

  private Long positionId;
  private String positionName;
  private String positionDescription;

  private BigDecimal salary;
  private String status;
  private LocalDate hireDate;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long createdBy;
  private Long updatedBy;
}
