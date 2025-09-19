package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.exceptions.*;
import com.sanisidro.restaurante.core.security.dto.*;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.facade.NotificationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final NotificationFacade notificationFacade;

    private final int MAX_ATTEMPTS_USER = 5;
    private final int MAX_ATTEMPTS_IP = 10;
    private final long BLOCK_TIME_USER = 15 * 60 * 1000;
    private final long BLOCK_TIME_IP = 15 * 60 * 1000;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipBlockedUntil = new ConcurrentHashMap<>();

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        String key = request.getUsernameOrEmail();

        if (blockedUntil.containsKey(key) && blockedUntil.get(key) > System.currentTimeMillis()) {
            throw new TooManyAttemptsException("Demasiados intentos fallidos. Intenta más tarde.");
        }

        if (ipBlockedUntil.containsKey(clientIp) && ipBlockedUntil.get(clientIp) > System.currentTimeMillis()) {
            throw new TooManyAttemptsException("Demasiados intentos desde esta IP. Intenta más tarde.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(key, request.getPassword())
            );

            // Reset de intentos fallidos
            loginAttempts.remove(key);
            blockedUntil.remove(key);
            ipLoginAttempts.remove(clientIp);
            ipBlockedUntil.remove(clientIp);

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            RefreshToken refreshToken = createRefreshToken(user);

            UserProfileResponse profile = new UserProfileResponse(
                    user.getUsername(),
                    user.getEmail(),
                    user.isEnabled(),
                    user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
            );

            return new AuthResponse(
                    jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap()),
                    refreshToken.getToken(),
                    profile
            );

        } catch (Exception ex) {
            trackFailedLogin(key, clientIp);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

    private void trackFailedLogin(String key, String clientIp) {
        int attemptsUser = loginAttempts.getOrDefault(key, 0) + 1;
        loginAttempts.put(key, attemptsUser);
        if (attemptsUser >= MAX_ATTEMPTS_USER) {
            blockedUntil.put(key, System.currentTimeMillis() + BLOCK_TIME_USER);
            loginAttempts.remove(key);
        }

        int attemptsIp = ipLoginAttempts.getOrDefault(clientIp, 0) + 1;
        ipLoginAttempts.put(clientIp, attemptsIp);
        if (attemptsIp >= MAX_ATTEMPTS_IP) {
            ipBlockedUntil.put(clientIp, System.currentTimeMillis() + BLOCK_TIME_IP);
            ipLoginAttempts.remove(clientIp);
        }
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username ya existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email ya existe");
        }

        Role defaultRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Rol cliente no encontrado"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Collections.singleton(defaultRole))
                .enabled(true)
                .emailVerified(false)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .verificationCode(UUID.randomUUID().toString())
                .build();

        userRepository.save(user);

        EmailVerificationEvent event = EmailVerificationEvent.builder()
                .userId(user.getId())
                .recipient(user.getEmail())
                .subject("Verifica tu correo electrónico")
                .message("Por favor verifica tu correo haciendo clic en el enlace:")
                .actionUrl("http://localhost:8080/api/v1/auth/verify?code=" + user.getVerificationCode())
                .build();

        notificationFacade.processNotification(event);

        return user;
    }

    @Transactional
    public User verifyEmail(String code) {
        User user = userRepository.findByVerificationCode(code)
                .orElseThrow(() -> new InvalidVerificationCodeException("Código de verificación inválido o expirado"));

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);

        return user;
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenStr, String clientIp, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token no válido"));

        if (!jwtService.validate(refreshToken.getToken())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expirado");
        }

        User user = refreshToken.getUser();

        tokenBlacklistService.blacklistToken(refreshToken.getToken(), user.getUsername(),
                "refresh", refreshTokenExpiration, clientIp, userAgent);

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap());
        refreshTokenRepository.delete(refreshToken);
        RefreshToken newRefreshToken = createRefreshToken(user);

        UserProfileResponse profile = new UserProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );

        return new AuthResponse(newAccessToken, newRefreshToken.getToken(), profile);
    }

    @Transactional
    public void logout(String refreshTokenStr, String accessToken, String clientIp, String userAgent) {
        refreshTokenRepository.deleteByToken(refreshTokenStr);

        long expirationMillis = jwtService.getExpirationMillis(accessToken);
        String username = jwtService.extractUsername(accessToken);

        tokenBlacklistService.blacklistToken(accessToken, username, "logout", expirationMillis, clientIp, userAgent);
    }

    @Transactional
    public void logoutAll(String accessToken, String clientIp, String userAgent) {
        if (!jwtService.validate(accessToken)) {
            throw new InvalidRefreshTokenException("Access token no válido o expirado");
        }

        String username = jwtService.extractUsername(accessToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        revokeAllRefreshTokens(user);

        long expirationMillis = jwtService.getExpirationMillis(accessToken);
        tokenBlacklistService.blacklistToken(accessToken, username, "logout_all", expirationMillis, clientIp, userAgent);
    }

    @Transactional
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtService.generateRefreshToken(user.getUsername()))
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    void revokeAllRefreshTokens(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}
