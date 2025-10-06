package com.sanisidro.restaurante.core.security.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sanisidro.restaurante.core.aws.model.FileMetadata;
import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.exceptions.EmailChangeNotAllowedException;
import com.sanisidro.restaurante.core.exceptions.InvalidPasswordException;
import com.sanisidro.restaurante.core.exceptions.UserNotFoundException;
import com.sanisidro.restaurante.core.exceptions.UsernameChangeNotAllowedException;
import com.sanisidro.restaurante.core.security.dto.ChanguePasswordRequest;
import com.sanisidro.restaurante.core.security.dto.UpdateProfileRequest;
import com.sanisidro.restaurante.core.security.dto.UpdateProfileResponse;
import com.sanisidro.restaurante.core.security.dto.UpdateUserRequest;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.dto.UserSessionResponse;
import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final NotificationProducer notificationProducer;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional(readOnly = true)
    public List<UserSessionResponse> getSessions(User user, String currentAccessToken) {
        Optional<RefreshToken> currentSession = authService.getRefreshTokenByAccessToken(currentAccessToken);

        String currentSessionId = currentSession.map(rt -> rt.getId().toString()).orElse("");

        return authService.getUserSessions(user, currentSessionId);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getUsersForAdmin(User requestingUser) {
        boolean isAdmin = requestingUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Acceso denegado: solo administradores");
        }

        Set<String> excludedRoles = Set.of("ROLE_CLIENT", "ROLE_SUPPLIER");

        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .map(Role::getName)
                        .noneMatch(excludedRoles::contains))
                .collect(Collectors.toList());

        return users.stream()
                .map(u -> getUserByUsername(u.getUsername()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(User requestingUser, Long userId) {
        boolean isAdmin = requestingUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Acceso denegado: solo administradores");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .provider(user.getProvider().name())
                .hasPassword(user.getPassword() != null && !user.getPassword().isEmpty())
                .profileImageUrl(
                        user.getProfileImageId() != null ? fileService.getFileUrl(user.getProfileImageId()) : null)
                .build();
    }

    @Transactional
    public UpdateProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        boolean loginChanged = false;
        boolean passwordRequired = false;

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        String newEmail = request.getEmail().toLowerCase();

        if (!newEmail.equals(user.getEmail().toLowerCase())) {

            if (user.getProvider() != AuthProvider.LOCAL
                    && (user.getPassword() == null || user.getPassword().isEmpty())) {
                passwordRequired = true;
                throw new InvalidPasswordException(
                        "Debes establecer una contraseña antes de cambiar tu correo.");
            }

            if (user.getLastEmailChange() != null) {
                LocalDateTime nextAllowedEmailChange = user.getLastEmailChange().plusDays(30);
                if (LocalDateTime.now().isBefore(nextAllowedEmailChange)) {
                    long daysLeft = java.time.Duration.between(LocalDateTime.now(), nextAllowedEmailChange).toDays();
                    throw new EmailChangeNotAllowedException(
                            "No puedes cambiar tu email aún. Te faltan " + daysLeft + " días.");
                }
            }

            if (userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new EmailChangeNotAllowedException("El email ya está en uso");
            }

            user.setEmail(newEmail);
            user.setLastEmailChange(LocalDateTime.now());
            user.setEmailVerified(false);

            if (user.getProvider() != AuthProvider.LOCAL) {
                user.setGoogleId(null);
                user.setFacebookId(null);
                user.setGithubId(null);
                user.setProvider(AuthProvider.LOCAL);
                passwordRequired = true;
            }

            String verificationCode = UUID.randomUUID().toString();
            user.setVerificationCode(verificationCode);

            userRepository.save(user);

            EmailVerificationEvent event = EmailVerificationEvent.builder()
                    .userId(user.getId())
                    .recipient(newEmail)
                    .subject("Verifica tu nuevo correo")
                    .message("Por favor verifica tu correo haciendo clic en el enlace:")
                    .actionUrl(frontendUrl + "/verify-email?code=" + verificationCode)
                    .build();
            notificationProducer.send("notifications", event);

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
        if (loginChanged && !passwordRequired) {
            newToken = jwtService.generateAccessToken(
                    user.getUsername(),
                    Map.of("roles", user.getRoles().stream().map(Role::getName).toList()));
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
    public UserProfileResponse updateUserById(User requestingUser, Long userId, UpdateUserRequest request) {
        boolean isAdmin = requestingUser.getRoles().stream()
                .map(Role::getName)
                .anyMatch(r -> r.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new AccessDeniedException("Acceso denegado: solo administradores pueden modificar usuarios.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setEnabled(request.isEnabled());

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> newRoles = request.getRoles().stream()
                    .map(roleName -> {
                        Role role = new Role();
                        role.setName(roleName);
                        return role;
                    })
                    .collect(Collectors.toSet());
            user.setRoles(newRoles);
        }

        userRepository.save(user);

        return getUserByUsername(user.getUsername());
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
                .id(user.getId())
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
