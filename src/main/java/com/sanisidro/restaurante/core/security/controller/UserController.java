package com.sanisidro.restaurante.core.security.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.core.security.dto.ChanguePasswordRequest;
import com.sanisidro.restaurante.core.security.dto.UpdateProfileRequest;
import com.sanisidro.restaurante.core.security.dto.UpdateProfileResponse;
import com.sanisidro.restaurante.core.security.dto.UserProfileResponse;
import com.sanisidro.restaurante.core.security.dto.UserSessionResponse;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(
            @AuthenticationPrincipal User user) {

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        UserProfileResponse profile = userService.getUserByUsername(user.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "Perfil obtenido correctamente", profile));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> getSessions(
            @AuthenticationPrincipal User user,
            @RequestHeader("Authorization") String authHeader) {

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        String accessToken = authHeader.substring(7);

        List<UserSessionResponse> sessions = userService.getSessions(user, accessToken);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Sesiones obtenidas correctamente", sessions));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<ApiResponse<UpdateProfileResponse>> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        UpdateProfileResponse updatedUser = userService.updateProfile(user, request);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Perfil actualizado correctamente", updatedUser));
    }

    @PutMapping("/profile-image")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfileImage(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        UserProfileResponse updatedProfile = userService.updateProfileImage(user, file);

        return ResponseEntity.ok(new ApiResponse<>(true, "Imagen de perfil actualizada", updatedProfile));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Object>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChanguePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Usuario no autenticado", null));
        }

        userService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Contraseña actualizada correctamente", null));
    }

}
