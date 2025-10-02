package com.sanisidro.restaurante.core.security.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.security.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.exceptions.EmailAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.InvalidCredentialsException;
import com.sanisidro.restaurante.core.exceptions.InvalidRefreshTokenException;
import com.sanisidro.restaurante.core.exceptions.InvalidVerificationCodeException;
import com.sanisidro.restaurante.core.exceptions.TooManyAttemptsException;
import com.sanisidro.restaurante.core.exceptions.UserNotFoundException;
import com.sanisidro.restaurante.core.exceptions.UsernameAlreadyExistsException;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final FileService fileService;

    private final NotificationProducer notificationProducer;

    private final int MAX_ATTEMPTS_USER = 5;
    private final int MAX_ATTEMPTS_IP = 10;
    private final long BLOCK_TIME_USER = 15 * 60 * 1000;
    private final long BLOCK_TIME_IP = 15 * 60 * 1000;

    private final Map<String, Integer> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> blockedUntil = new ConcurrentHashMap<>();
    private final Map<String, Integer> ipLoginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Long> ipBlockedUntil = new ConcurrentHashMap<>();

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        String key = request.getUsernameOrEmail();

        if (blockedUntil.containsKey(key) && blockedUntil.get(key) > System.currentTimeMillis()) {
            throw new TooManyAttemptsException("Demasiados intentos fallidos. Intenta más tarde.");
        }

        if (ipBlockedUntil.containsKey(clientIp) && ipBlockedUntil.get(clientIp) > System.currentTimeMillis()) {
            throw new TooManyAttemptsException("Demasiados intentos desde esta IP. Intenta más tarde.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(key, request.getPassword()));

            loginAttempts.remove(key);
            blockedUntil.remove(key);
            ipLoginAttempts.remove(clientIp);
            ipBlockedUntil.remove(clientIp);

            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

            RefreshToken refreshToken = createRefreshToken(user, clientIp, userAgent);

            Set<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());

            UserProfileResponse profile = buildUserProfileResponse(user);

            return new AuthResponse(
                    jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap()),
                    refreshToken.getId().toString(),
                    profile);

        } catch (Exception ex) {
            trackFailedLogin(key, clientIp);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional
    public Long register(RegisterRequest request) {
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
                .actionUrl(frontendUrl + "/verify-email?code=" + user.getVerificationCode())
                .build();

        notificationProducer.send("notifications", event);

        return user.getId();
    }

    @Transactional
    public void verifyEmail(String code) {
        User user = userRepository.findByVerificationCode(code)
                .orElseThrow(() -> new InvalidVerificationCodeException("Código de verificación inválido o expirado"));

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
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

        RefreshToken newRefreshToken = createRefreshToken(user, clientIp, userAgent);

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        UserProfileResponse profile = buildUserProfileResponse(user);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken.getId().toString(),
                profile);
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
        tokenBlacklistService.blacklistToken(accessToken, username, "logout_all", expirationMillis, clientIp,
                userAgent);
    }

    @Transactional
    public void logout(String sessionId, String accessToken, String clientIp, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new InvalidRefreshTokenException("Sesión no encontrada"));

        refreshTokenRepository.delete(refreshToken);

        long expirationMillis = jwtService.getExpirationMillis(accessToken);
        String username = jwtService.extractUsername(accessToken);

        tokenBlacklistService.blacklistToken(accessToken, username, "logout", expirationMillis, clientIp, userAgent);
    }

    @Transactional
    private RefreshToken createRefreshToken(User user, String clientIp, String userAgent) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtService.generateRefreshToken(user.getUsername()))
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .ip(clientIp)
                .userAgent(userAgent)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    void revokeAllRefreshTokens(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Transactional(readOnly = true)
    public List<UserSessionResponse> getUserSessions(User user, String currentSessionId) {
        return refreshTokenRepository.findAllByUser(user).stream()
                .filter(rt -> !rt.getId().toString().equals(currentSessionId))
                .map(rt -> new UserSessionResponse(
                        rt.getId().toString(),
                        rt.getExpiryDate(),
                        rt.getIp(),
                        rt.getUserAgent()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RefreshToken> getRefreshTokenByAccessToken(String accessToken) {
        String username = jwtService.extractUsername(accessToken);
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUser(userRepository.findByUsername(username).get());
        return tokens.stream().max(Comparator.comparing(RefreshToken::getExpiryDate));
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

    private UserProfileResponse buildUserProfileResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String profileImageUrl = null;
        if (user.getProfileImageId() != null) {
            try {
                profileImageUrl = fileService.getFileUrl(user.getProfileImageId());
            } catch (Exception e) {
                profileImageUrl = null; // Si falla, dejamos null
            }
        }

        LocalDateTime usernameNextChange = null;
        if (user.getLastUsernameChange() != null) {
            usernameNextChange = user.getLastUsernameChange().plusDays(7);
        }

        LocalDateTime emailNextChange = null;
        if (user.getLastEmailChange() != null) {
            emailNextChange = user.getLastEmailChange().plusDays(30);
        }

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(roles)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .provider(user.getProvider().name())
                .hasPassword(user.getPassword() != null && !user.getPassword().isEmpty())
                .profileImageUrl(profileImageUrl)
                .usernameNextChange(usernameNextChange)
                .emailNextChange(emailNextChange)
                .build();
    }


}
