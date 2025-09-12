package com.sanisidro.restaurante.core.security.controller;

import com.sanisidro.restaurante.core.security.dto.*;
import com.sanisidro.restaurante.core.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        ApiResponse<AuthResponse> response = authService.login(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<Object> response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        ApiResponse<AuthResponse> response = authService.refresh(request.getRefreshToken(), clientIp, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Access token requerido", null));
        }

        String accessToken = authHeader.substring(7);
        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<Object> response = authService.logout(request.getRefreshToken(), accessToken, clientIp, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Object>> logoutAll(HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Access token requerido", null));
        }

        String accessToken = authHeader.substring(7);
        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiResponse<Object> response = authService.logoutAll(accessToken, clientIp, userAgent);
        return ResponseEntity.ok(response);
    }
}
