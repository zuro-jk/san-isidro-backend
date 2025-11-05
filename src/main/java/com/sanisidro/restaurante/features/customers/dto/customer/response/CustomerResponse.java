package com.sanisidro.restaurante.features.customers.dto.customer.response;

import java.time.LocalDateTime;
import java.util.Set;

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
public class CustomerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Integer points;

    private Boolean enabled;
    private Boolean emailVerified;
    private Set<String> roles;
    private String provider;
    private String profileImageUrl;
    private LocalDateTime lastUsernameChange;
    private LocalDateTime lastEmailChange;
}