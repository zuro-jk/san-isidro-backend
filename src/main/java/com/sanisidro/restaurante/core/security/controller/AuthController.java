package com.sanisidro.restaurante.core.security.controller;

import com.mercadopago.MercadoPagoConfig;
import com.sanisidro.restaurante.core.exceptions.InvalidVerificationCodeException;
import com.sanisidro.restaurante.core.security.dto.*;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/verify")
    public void verifyEmail(@RequestParam String code, HttpServletResponse response) throws IOException {
        authService.verifyEmail(code);
//        MercadoPagoConfig.setAccessToken("CREDENCIAL");

        response.sendRedirect("http://localhost:3000/login?verified=true");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String clientIp = httpRequest.getRemoteAddr();
        AuthResponse authResponse = authService.login(request, clientIp);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login exitoso", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Usuario registrado exitosamente. Revisa tu correo para activar la cuenta.",
                user.getId()));
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

        authService.logout(request.getRefreshToken(), accessToken, clientIp, userAgent);
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
