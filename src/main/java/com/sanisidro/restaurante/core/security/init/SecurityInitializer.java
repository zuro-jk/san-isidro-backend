package com.sanisidro.restaurante.core.security.init;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.core.security.enums.AuthProvider;
import com.sanisidro.restaurante.core.security.model.PaymentProfile;
import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class SecurityInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
        initBaseEmployeeUsers();
        initSupplierUsers();
        initTestCustomerUsers();
        initDefaultCustomerUser();
    }

    private void initRoles() {
        if (roleRepository.count() > 0) {
            log.info(">>> Roles ya inicializados.");
            return;
        }
        log.info(">>> Inicializando Roles...");
        String[] roles = { "ROLE_CLIENT", "ROLE_WAITER", "ROLE_CHEF", "ROLE_CASHIER", "ROLE_ADMIN", "ROLE_SUPPLIER",
                "ROLE_MANAGER" };
        for (String roleName : roles) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
        }
        log.info(">>> Roles inicializados correctamente.");
    }

    private void initAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info(">>> Usuario admin ya existe.");
            return;
        }
        log.info(">>> Creando usuario admin...");
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no encontrado"));

        User admin = User.builder()
                .username("admin")
                .email("DarckProyect8@gmail.com")
                .firstName("Admin")
                .lastName("User")
                .password(passwordEncoder.encode("admin123"))
                .roles(Collections.singleton(adminRole))
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                .build();
        userRepository.save(admin);
        log.info(">>> Usuario admin creado: admin / admin123");
    }

    private void initBaseEmployeeUsers() {
        log.info(">>> Creando usuarios base para futuros empleados...");
        Role defaultRole = roleRepository.findByName("ROLE_CLIENT").orElse(null); // Opcional

        createUserIfNotExistsBase("jose_m", "jose.manager@example.com", "José", "Lopez", "password123",
                defaultRole != null ? Set.of(defaultRole) : Set.of());
        createUserIfNotExistsBase("maria_w", "maria.waiter@example.com", "María", "Pérez", "password123",
                defaultRole != null ? Set.of(defaultRole) : Set.of());
        createUserIfNotExistsBase("carlos_c", "carlos.chef@example.com", "Carlos", "Ramírez", "password123",
                defaultRole != null ? Set.of(defaultRole) : Set.of());
        log.info(">>> Usuarios base para futuros empleados creados.");
    }

    private void initSupplierUsers() {
        log.info(">>> Creando usuarios base para proveedores...");
        Role supplierRole = roleRepository.findByName("ROLE_SUPPLIER").orElseThrow();

        if (userRepository.findByEmail("sanfernando@example.com").isEmpty()) {
            User user = User.builder()
                    .username("sanfernando_user")
                    .firstName("Carlos")
                    .lastName("Ramirez")
                    .email("sanfernando@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .roles(Set.of(supplierRole))
                    .enabled(true).emailVerified(true)
                    .provider(AuthProvider.LOCAL).build();
            userRepository.save(user);
        }
        if (userRepository.findByEmail("gloria@example.com").isEmpty()) {
            User user = User.builder()
                    .username("gloria_user")
                    .firstName("Ana")
                    .lastName("Torres")
                    .email("gloria@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .roles(Set.of(supplierRole))
                    .enabled(true).emailVerified(true)
                    .provider(AuthProvider.LOCAL).build();
            userRepository.save(user);
        }
        log.info(">>> Usuarios base para proveedores creados.");
    }

    private void initTestCustomerUsers() {
        log.info(">>> Creando usuarios base para clientes de prueba...");
        Role clientRole = roleRepository.findByName("ROLE_CLIENT").orElseThrow();

        if (userRepository.findByUsername("cliente1").isEmpty()) {
            User user1 = User.builder()
                    .username("cliente1")
                    .email("cliente1@test.com")
                    .firstName("Juan")
                    .lastName("Lopez")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true).emailVerified(true)
                    .roles(Set.of(clientRole))
                    .provider(AuthProvider.LOCAL).build();
            PaymentProfile profile1 = PaymentProfile.builder().user(user1).docType("DNI").docNumber("12345678").build();
            user1.setPaymentProfile(profile1);
            userRepository.save(user1);
        }
        if (userRepository.findByUsername("cliente2").isEmpty()) {
            User user2 = User.builder()
                    .username("cliente2")
                    .email("cliente2@test.com")
                    .firstName("Ana")
                    .lastName("Martinez")
                    .password(passwordEncoder.encode("password123"))
                    .enabled(true).emailVerified(true)
                    .roles(Set.of(clientRole))
                    .provider(AuthProvider.LOCAL).build();
            PaymentProfile profile2 = PaymentProfile.builder().user(user2).docType("DNI").docNumber("87654321").build();
            user2.setPaymentProfile(profile2);
            userRepository.save(user2);
        }
        log.info(">>> Usuarios base para clientes de prueba creados.");
    }

    private void createUserIfNotExistsBase(String username, String email, String firstName, String lastName,
            String password, Set<Role> initialRoles) {
        userRepository.findByUsername(username).orElseGet(() -> {
            log.debug("Creando usuario base: {}", username);
            User newUser = User.builder()
                    .username(username)
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .password(passwordEncoder.encode(password))
                    .enabled(true)
                    .emailVerified(true)
                    .provider(AuthProvider.LOCAL)
                    .roles(initialRoles)
                    .build();
            return userRepository.save(newUser);
        });
    }

    private void initDefaultCustomerUser() {
        final String DEFAULT_USERNAME = "publico_general";

        if (userRepository.findByUsername(DEFAULT_USERNAME).isPresent()) {
            log.info(">>> Usuario 'publico_general' ya existe.");
            return;
        }
        log.info(">>> Creando usuario 'publico_general'...");

        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new IllegalStateException("ROLE_CLIENT no encontrado"));

        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User defaultUser = User.builder()
                .username(DEFAULT_USERNAME)
                .email("ventas@" + System.currentTimeMillis() + ".com")
                .firstName("Público")
                .lastName("General")
                .password(randomPassword)
                .roles(Collections.singleton(clientRole))
                .provider(AuthProvider.LOCAL)
                .enabled(true)
                .emailVerified(true)
                .build();

        userRepository.save(defaultUser);
        log.info(">>> Usuario 'publico_general' creado.");
    }
}