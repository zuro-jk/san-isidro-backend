package com.sanisidro.restaurante.core.security.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.aws.service.FileService;
import com.sanisidro.restaurante.core.exceptions.EmailAlreadyExistsException;
import com.sanisidro.restaurante.core.exceptions.InvalidCredentialsException;
import com.sanisidro.restaurante.core.exceptions.InvalidRefreshTokenException;
import com.sanisidro.restaurante.core.exceptions.InvalidVerificationCodeException;
import com.sanisidro.restaurante.core.exceptions.TooManyAttemptsException;
import com.sanisidro.restaurante.core.exceptions.UserNotFoundException;
import com.sanisidro.restaurante.core.exceptions.UsernameAlreadyExistsException;
import com.sanisidro.restaurante.core.security.dto.AuthResponse;
import com.sanisidro.restaurante.core.security.dto.LoginRequest;
import com.sanisidro.restaurante.core.security.dto.RegisterRequest;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.dto.UserSessionResponse;
import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.enums.TokenEventType;
import com.sanisidro.restaurante.core.security.jwt.JwtService;
import com.sanisidro.restaurante.core.security.model.RefreshToken;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.TokenAudit;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RefreshTokenRepository;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.TokenAuditRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.dto.customer.request.CustomerRequest;
import com.sanisidro.restaurante.features.customers.service.CustomerService;
import com.sanisidro.restaurante.features.employees.errors.OutOfScheduleAccessException;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.service.ScheduleService;
import com.sanisidro.restaurante.features.notifications.dto.EmailVerificationEvent;
import com.sanisidro.restaurante.features.notifications.kafka.NotificationProducer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
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
    private final CustomerService customerService;
    private final TokenAuditRepository tokenAuditRepository;
    private final ScheduleService scheduleService;
    private final EmployeeRepository employeeRepository;

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

            boolean esAdmin = user.hasRole("ROLE_ADMIN");
            boolean esCliente = user.hasRole("ROLE_CLIENT");

            log.info("Iniciando login para: {}. Es Admin: {}. Es Cliente: {}", user.getUsername(), esAdmin, esCliente);

            if (!esAdmin && !esCliente) {
                log.info("Usuario {} es un empleado. Verificando horario...", user.getUsername());

                var employeeOpt = employeeRepository.findByUserId(user.getId());

                if (employeeOpt.isPresent()) {
                    Employee employee = employeeOpt.get();
                    log.info("Verificando horario para Employee ID: {}", employee.getId());

                    boolean dentroDeHorario = scheduleService.isWithinSchedule(employee);

                    log.info("Resultado de isWithinSchedule: {}", dentroDeHorario);

                    if (!dentroDeHorario) {
                        log.warn("Acceso denegado para {}. Razón: Fuera de horario.", user.getUsername());
                        throw new OutOfScheduleAccessException("No puedes iniciar sesión fuera de tu horario laboral");
                    }

                    log.info("Acceso permitido para {}. Está dentro de horario.", user.getUsername());

                } else {
                    log.warn("Usuario {} tiene roles de empleado pero no existe una entidad 'Employee' asociada.",
                            user.getUsername());
                }
            }

            RefreshToken refreshToken = createRefreshToken(user, clientIp, userAgent);
            String accessToken = jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap());
            Instant accessTokenExpiration = jwtService.getExpirationInstant(accessToken);

            TokenAudit audit = TokenAudit.builder()
                    .token(accessToken)
                    .username(user.getUsername())
                    .eventType(TokenEventType.ISSUED)
                    .timestamp(Instant.now())
                    .expiresAt(accessTokenExpiration)
                    .reason("Inicio de sesión exitoso")
                    .ipAddress(clientIp)
                    .userAgent(userAgent)
                    .build();
            tokenAuditRepository.save(audit);

            UserProfileResponse profile = buildUserProfileResponse(user);

            return new AuthResponse(accessToken, refreshToken.getId().toString(), profile);
        } catch (OutOfScheduleAccessException e) {
            throw e;
        } catch (SecurityException e) {
            log.error("SecurityException durante el login de {}", key, e);
            throw new OutOfScheduleAccessException("No puedes iniciar sesión fuera de tu horario laboral");
        } catch (Exception ex) {
            log.error("Excepción genérica durante el login de {}", key, ex);
            trackFailedLogin(key, clientIp);
            throw new InvalidCredentialsException("Credenciales inválidas");
        }
    }

    @Transactional
    public Long register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username ya existe");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
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
                .provider(AuthProvider.LOCAL)
                .build();

        userRepository.save(user);

        CustomerRequest dto = CustomerRequest.builder()
                .userId(user.getId())
                .points(10)
                .build();

        customerService.createCustomer(dto);

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
                "refresh", refreshToken.getExpiryDate(), clientIp, userAgent);

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), Collections.emptyMap());
        refreshTokenRepository.delete(refreshToken);

        RefreshToken newRefreshToken = createRefreshToken(user, clientIp, userAgent);

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

        Instant expiresAt = jwtService.getExpirationInstant(accessToken);

        tokenBlacklistService.blacklistToken(
                accessToken,
                username,
                "logout_all",
                expiresAt,
                clientIp,
                userAgent);
    }

    @Transactional
    public void logout(String sessionId, String accessToken, String clientIp, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findById(Long.parseLong(sessionId))
                .orElseThrow(() -> new InvalidRefreshTokenException("Sesión no encontrada"));

        refreshTokenRepository.delete(refreshToken);

        String username = jwtService.extractUsername(accessToken);

        tokenBlacklistService.blacklistToken(accessToken, username, "logout", refreshToken.getExpiryDate(), clientIp,
                userAgent);
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
                        rt.getUserAgent()))
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
