package com.sanisidro.restaurante.core.security.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.security.dto.AuthResponse;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthUserService {

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FileService fileService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse processOAuthUser(String provider, String providerId,
            String email, String firstName, String lastName,
            Boolean emailVerified, String profileImageUrl) {

        String normalizedEmail = email.toLowerCase();

        Optional<User> userOpt = findByProviderId(provider, providerId);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            Optional<User> userByEmail = userRepository.findByEmailIgnoreCase(normalizedEmail);

            if (userByEmail.isPresent()) {
                // Usuario existente con email, actualizar provider
                user = userByEmail.get();

                switch (provider.toLowerCase()) {
                    case "google" -> {
                        user.setProvider(AuthProvider.GOOGLE);
                        user.setGoogleId(providerId);
                    }
                    case "facebook" -> {
                        user.setProvider(AuthProvider.FACEBOOK);
                        user.setFacebookId(providerId);
                    }
                    case "github" -> {
                        user.setProvider(AuthProvider.GITHUB);
                        user.setGithubId(providerId);
                    }
                }

                userRepository.save(user);
            } else {
                // Crear nuevo usuario OAuth
                user = createUserFromOAuth(provider, providerId, normalizedEmail,
                        firstName, lastName, emailVerified, profileImageUrl);
            }
        }

        // Generar tokens
        String accessToken = jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("roles", user.getRoles().stream().map(Role::getName).toList()));

        RefreshToken refreshTokenEntity = createRefreshToken(user);

        // Construir profile image
        String finalProfileImageUrl = profileImageUrl;
        if (user.getProfileImageId() != null) {
            try {
                finalProfileImageUrl = fileService.getFileUrl(user.getProfileImageId());
            } catch (Exception e) {
                log.warn("No se pudo obtener URL de S3, usando URL del proveedor: {}", e.getMessage());
            }
        }

        // Construir response
        UserProfileResponse userProfile = UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .phone(user.getPhone())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .hasPassword(user.getPassword() != null && !user.getPassword().isEmpty())
                .profileImageUrl(finalProfileImageUrl)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .sessionId(refreshTokenEntity.getId().toString())
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

    private User createUserFromOAuth(String provider, String providerId, String email,
            String firstName, String lastName, Boolean emailVerified,
            String profileImageUrl) {

        User user = new User();
        user.setEmail(email);

        // Generar username único para evitar duplicados
        String baseUsername = provider.toLowerCase() + "_" + providerId;
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter++;
        }
        user.setUsername(username);

        // Nombres
        if (firstName == null && lastName == null) {
            user.setFirstName(email.split("@")[0]);
            user.setLastName("");
        } else if (lastName == null && firstName != null && firstName.contains(" ")) {
            String[] parts = firstName.split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts[1]);
        } else {
            user.setFirstName(firstName != null ? firstName : email.split("@")[0]);
            user.setLastName(lastName != null ? lastName : "");
        }

        user.setEnabled(true);
        user.setPassword(null);
        user.setEmailVerified(emailVerified != null && emailVerified);

        // Provider
        switch (provider.toLowerCase()) {
            case "google" -> {
                user.setProvider(AuthProvider.GOOGLE);
                user.setGoogleId(providerId);
            }
            case "facebook" -> {
                user.setProvider(AuthProvider.FACEBOOK);
                user.setFacebookId(providerId);
            }
            case "github" -> {
                user.setProvider(AuthProvider.GITHUB);
                user.setGithubId(providerId);
            }
            default -> {
                user.setProvider(AuthProvider.LOCAL);
                log.warn("Provider OAuth2 no soportado: {}", provider);
            }
        }

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("ROLE_CLIENT no existe"));
        user.setRoles(Set.of(clientRole));

        user.setProfileImageId(null);

        User savedUser = userRepository.save(user);

        Customer customer = Customer.builder()
                .points(10)
                .user(savedUser)
                .build();
        customerRepository.save(customer);

        log.info("Nuevo usuario OAuth creado: {} (provider={})", email, provider);

        return savedUser;
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