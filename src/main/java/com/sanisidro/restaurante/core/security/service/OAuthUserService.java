package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OAuthUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    public String processOAuthUser(String provider, String providerId, String email, String firstName, String lastName, Boolean emailVerified) {
        User user = findByProviderId(provider, providerId)
                .orElseGet(() -> createUserFromOAuth(provider, providerId, email, firstName, lastName, emailVerified));

        return jwtService.generateAccessToken(
                user.getUsername(),
                Map.of("roles", user.getRoles().stream().map(Role::getName).toList())
        );
    }

    private Optional<User> findByProviderId(String provider, String providerId) {
        return switch (provider.toLowerCase()) {
            case "google" -> userRepository.findByGoogleId(providerId);
            case "facebook" -> userRepository.findByFacebookId(providerId);
            case "github" -> userRepository.findByGithubId(providerId);
            default -> java.util.Optional.empty();
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
        }

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("ROLE_CLIENT no existe"));
        user.setRoles(Set.of(clientRole));

        return userRepository.save(user);
    }
}