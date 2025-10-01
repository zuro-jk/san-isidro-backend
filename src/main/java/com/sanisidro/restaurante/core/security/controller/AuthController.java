package com.sanisidro.restaurante.core.security.controller;

import com.sanisidro.restaurante.core.security.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sanisidro.restaurante.core.exceptions.InvalidVerificationCodeException;
import com.sanisidro.restaurante.core.security.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String code) {
        try {
            authService.verifyEmail(code);
            return ResponseEntity.ok("Correo verificado exitosamente");
        } catch (InvalidVerificationCodeException e) {
            return ResponseEntity.badRequest().body("Código inválido o expirado");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse authResponse = authService.login(request, httpRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login exitoso", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Se ha registrado correctamente. Revisa tu correo para activar la cuenta.",
                userId));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthResponse response = authService.refresh(request.getRefreshToken(), clientIp, userAgent);
        return ResponseEntity.ok(new ApiResponse<>(true, "Refresh exitoso", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @Valid @RequestBody SessionLogoutRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Access token requerido", null));
        }

        String accessToken = authHeader.substring(7);
        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        authService.logout(request.getSessionId(), accessToken, clientIp, userAgent);

        return ResponseEntity.ok(new ApiResponse<>(true, "Logout exitoso", null));
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

        authService.logoutAll(accessToken, clientIp, userAgent);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sesiones cerradas en todos los dispositivos", null));
    }
}
