package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        var oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email"); // Asegúrate que user-name-attribute=email

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Genera el token JWT
        String token = jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap());

        // Crea cookie segura con SameSite
        ResponseCookie jwtCookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)          // localhost sin HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")        // Lax funciona para redirección localhost->localhost
                .build();
        response.addHeader("Set-Cookie", jwtCookie.toString());

        // Redirige al frontend
        response.sendRedirect("http://localhost:4200/");
    }
}