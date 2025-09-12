package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.dto.AuthResponse;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthUserService {

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse processOAuthUser(String provider, String providerId,
                                         String email, String firstName, String lastName,
                                         Boolean emailVerified) {

        User user = findByProviderId(provider, providerId)
                .orElseGet(() -> createUserFromOAuth(provider, providerId, email, firstName, lastName, emailVerified));

        // Access token
        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("roles", user.getRoles().stream().map(Role::getName).toList())
        );

        // Refresh token persistido en DB
        RefreshToken refreshTokenEntity = createRefreshToken(user);

        UserProfileResponse userProfile = UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenEntity.getToken())
                .user(userProfile)
                .build();
    }

    private Optional<User> findByProviderId(String provider, String providerId) {
        return switch (provider.toLowerCase()) {
            case "google" -> userRepository.findByGoogleId(providerId);
            case "facebook" -> userRepository.findByFacebookId(providerId);
            case "github" -> userRepository.findByGithubId(providerId);
            default -> Optional.empty();
        };
    }

    private User createUserFromOAuth(String provider, String providerId, String email, String firstName, String lastName, Boolean emailVerified) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setPassword(null);
        user.setEmailVerified(emailVerified != null && emailVerified);

        switch (provider.toLowerCase()) {
            case "google" -> { user.setGoogleUser(true); user.setGoogleId(providerId); }
            case "facebook" -> user.setFacebookId(providerId);
            case "github" -> user.setGithubId(providerId);
            default -> log.warn("Provider OAuth2 no soportado: {}", provider);
        }

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("ROLE_CLIENT no existe"));
        user.setRoles(Set.of(clientRole));

        log.info("Creando nuevo usuario OAuth2: {} (provider={})", email, provider);
        return userRepository.save(user);
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtService.generateRefreshToken(user.getUsername()))
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}