package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceTest {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private CustomOAuth2UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        service = new CustomOAuth2UserService(userRepository, roleRepository);
    }

    @Test
    void testRegisterNewUser() {
        // Simular rol por defecto
        Role clientRole = new Role();
        clientRole.setName("ROLE_CLIENT");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));

        // Simular que no existe usuario
        when(userRepository.findByEmail("nuevo@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Simular OAuth2User de Google
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of(
                        "email", "nuevo@gmail.com",
                        "given_name", "Nuevo",
                        "family_name", "Usuario"
                ),
                "email"
        );

        User user = service.registerOrLoadUser(oAuth2User);

        // Verificar que se guardó un nuevo usuario
        verify(userRepository, times(1)).save(any(User.class));
        System.out.println("Usuario nuevo registrado correctamente: " + user.getEmail());
    }

    @Test
    void testLoadExistingUser() {
        // Simular rol por defecto
        Role clientRole = new Role();
        clientRole.setName("ROLE_CLIENT");
        when(roleRepository.findByName("ROLE_CLIENT")).thenReturn(Optional.of(clientRole));

        // Simular usuario existente
        User existingUser = User.builder()
                .email("existente@gmail.com")
                .username("existente")
                .firstName("Existente")
                .lastName("Usuario")
                .isGoogleUser(true)
                .emailVerified(true)
                .enabled(true)
                .roles(Collections.singleton(clientRole))
                .build();

        when(userRepository.findByEmail("existente@gmail.com")).thenReturn(Optional.of(existingUser));

        // Simular OAuth2User
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                Map.of(
                        "email", "existente@gmail.com",
                        "given_name", "Existente",
                        "family_name", "Usuario"
                ),
                "email"
        );

        User user = service.registerOrLoadUser(oAuth2User);

        // Verificar que NO se guardó un nuevo usuario
        verify(userRepository, times(0)).save(any(User.class));
        System.out.println("Usuario existente cargado correctamente: " + user.getEmail());
    }
}
