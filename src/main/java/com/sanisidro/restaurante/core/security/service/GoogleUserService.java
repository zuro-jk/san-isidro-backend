package com.sanisidro.restaurante.core.security.service;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GoogleUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository; // Si tienes roles separados

    public UserDetails createUserFromGoogle(String email, String name) {
        // 1️⃣ Crear entidad User
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);

        String[] parts = name.split(" ", 2);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : "");

        // 2️⃣ Asignar un rol por defecto (CLIENT)
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("ROLE_CLIENT no existe"));
        user.setRoles(Set.of(clientRole));

        // 3️⃣ Contraseña dummy (Google no la usa, pero UserDetails la necesita)
        user.setPassword(passwordEncoder.encode("DUMMY_PASSWORD"));

        // 4️⃣ Guardar en BD
        userRepository.save(user);

        // 5️⃣ Devolver UserDetails
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .toList()
        );
    }

}
