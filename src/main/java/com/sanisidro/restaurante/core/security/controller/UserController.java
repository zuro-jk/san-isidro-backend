package com.sanisidro.restaurante.core.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.dto.ChanguePasswordRequest;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        UserProfileResponse profile = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Perfil obtenido correctamente", profile));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChanguePasswordRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contraseña actualizada correctamente", null));
    }

}
