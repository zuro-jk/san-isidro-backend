package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User>{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("🚀 LoadUser llamado, registrando OAuth2UserRequest: " + userRequest);
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        System.out.println("Email recibido de Google: " + oAuth2User.getAttribute("email"));

        User user = registerOrLoadUser(oAuth2User);

        System.out.println("Usuario cargado/creado: " + user.getEmail());

        return new DefaultOAuth2User(
                user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList(),
                oAuth2User.getAttributes(),
                "email"
        );
    }

    // Nuevo método para test
    public User registerOrLoadUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        return userRepository.findByEmail(email).orElseGet(() -> {
            Role defaultRole = roleRepository.findByName("ROLE_CLIENT")
                    .orElseThrow(() -> new RuntimeException("ROL CLIENTE NO ENCONTRADO"));

            User newUser = User.builder()
                    .email(email)
                    .username(email.split("@")[0])
                    .firstName(oAuth2User.getAttribute("given_name"))
                    .lastName(oAuth2User.getAttribute("family_name"))
                    .isGoogleUser(true)
                    .emailVerified(true)
                    .enabled(true)
                    .roles(Collections.singleton(defaultRole))
                    .build();

            return userRepository.save(newUser);
        });
    }
}