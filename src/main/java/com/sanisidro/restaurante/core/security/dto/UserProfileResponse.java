package com.sanisidro.restaurante.core.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phone;
    private String provider;
    private boolean hasPassword;

}
