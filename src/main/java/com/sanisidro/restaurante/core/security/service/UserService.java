package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.aws.model.FileMetadata;
import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.exceptions.EmailChangeNotAllowedException;
import com.sanisidro.restaurante.core.exceptions.InvalidPasswordException;
import com.sanisidro.restaurante.core.exceptions.UserNotFoundException;
import com.sanisidro.restaurante.core.exceptions.UsernameChangeNotAllowedException;
import com.sanisidro.restaurante.core.security.dto.*;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<UserSessionResponse> getSessions(User user, String currentAccessToken) {
        Optional<RefreshToken> currentSession = authService.getRefreshTokenByAccessToken(currentAccessToken);

        String currentSessionId = currentSession.map(rt -> rt.getId().toString()).orElse("");

        return authService.getUserSessions(user, currentSessionId);
    }

    @Transactional
    public UpdateProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        boolean loginChanged = false;

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        if (!request.getEmail().equals(user.getEmail())) {
            if (user.getLastEmailChange() != null) {
                LocalDateTime nextAllowedEmailChange = user.getLastEmailChange().plusDays(30);
                if (LocalDateTime.now().isBefore(nextAllowedEmailChange)) {
                    long daysLeft = java.time.Duration.between(LocalDateTime.now(), nextAllowedEmailChange).toDays();
                    throw new EmailChangeNotAllowedException(
                            "No puedes cambiar tu email aún. Te faltan " + daysLeft + " días.");
                }
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailChangeNotAllowedException("El email ya está en uso");
            }
            user.setEmail(request.getEmail());
            user.setLastEmailChange(LocalDateTime.now());
            user.setEmailVerified(false);
            loginChanged = true;
        }

        if (!request.getUsername().equals(user.getUsername())) {
            if (user.getLastUsernameChange() != null) {
                LocalDateTime nextAllowedUsernameChange = user.getLastUsernameChange().plusDays(7);
                if (LocalDateTime.now().isBefore(nextAllowedUsernameChange)) {
                    long daysLeft = java.time.Duration.between(LocalDateTime.now(), nextAllowedUsernameChange).toDays();
                    throw new UsernameChangeNotAllowedException(
                            "No puedes cambiar tu username aún. Te faltan " + daysLeft + " días.");
                }
            }
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new UsernameChangeNotAllowedException("El username ya está en uso");
            }
            user.setUsername(request.getUsername());
            user.setLastUsernameChange(LocalDateTime.now());
            loginChanged = true;
        }

        userRepository.save(user);

        UserProfileResponse userProfile = getUserByUsername(user.getUsername());

        String newToken = null;
        if (loginChanged) {
            newToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    Map.of("roles", user.getRoles().stream().map(Role::getName).toList())
            );
        }

        return UpdateProfileResponse.builder()
                .user(userProfile)
                .token(newToken)
                .build();
    }

    @Transactional
    public UserProfileResponse updateProfileImage(User user, MultipartFile file) {
        FileMetadata metadata = fileService.uploadFile(file, "profiles");

        if (user.getProfileImageId() != null) {
            fileService.deleteFile(user.getProfileImageId());
        }

        user.setProfileImageId(metadata.getId());
        userRepository.save(user);

        UserProfileResponse profile = getUserByUsername(user.getUsername());
        profile.setProfileImageUrl(metadata.getUrl());
        return profile;
    }

    @Transactional
    public void changePassword(String username, ChanguePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (request.getCurrentPassword() == null ||
                    !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new InvalidPasswordException("La contraseña actual es incorrecta");
            }
        } else {
            if (request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty()) {
                throw new InvalidPasswordException(
                        "No necesitas contraseña actual para usuarios OAuth sin password");
            }
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        authService.revokeAllRefreshTokens(user);
    }

    public UserProfileResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String profileImageUrl = null;
        if (user.getProfileImageId() != null) {
            try {
                profileImageUrl = fileService.getFileUrl(user.getProfileImageId());
            } catch (Exception e) {
                profileImageUrl = null;
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
