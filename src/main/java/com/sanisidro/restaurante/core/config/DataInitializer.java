package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String[] roles = {
                "ROLE_CLIENT",
                "ROLE_WAITER",
                "ROLE_CHEF",
                "ROLE_CASHIER",
                "ROLE_ADMIN",
                "ROLE_SUPPLIER"
        };

        for (String roleName : roles) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
        }

        // Crear usuario admin inicial si no existe
        if (userRepository.findByUsername("admin").isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();

            User admin = User.builder()
                    .username("admin")
                    .email("DarckProyect8@gmail.com")
                    .firstName("Joe")
                    .lastName("Luna")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Collections.singleton(adminRole))
                    .enabled(true)
                    .build();

            userRepository.save(admin);
            System.out.println(">>> Usuario admin creado: admin / admin123");
        }
    }

}
