package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.exceptions.InvalidPasswordException;
import com.sanisidro.restaurante.core.exceptions.UserNotFoundException;
import com.sanisidro.restaurante.core.security.dto.ChanguePasswordRequest;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final UserRepository userRepository;


    public UserProfileResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(roles)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isGoogleUser(user.isGoogleUser())
                .googleId(user.getGoogleId())
                .facebookId(user.getFacebookId())
                .githubId(user.getGithubId())
                .build();
    }

    @Transactional
    public void changePassword(String username, ChanguePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        authService.revokeAllRefreshTokens(user);
    }

}
