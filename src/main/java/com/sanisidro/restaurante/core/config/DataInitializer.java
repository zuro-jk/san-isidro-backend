package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LoyaltyRuleRepository loyaltyRuleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
        initLoyaltyRules();
    }

    private void initRoles() {
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
        System.out.println(">>> Roles inicializados");
    }

    private void initAdminUser() {
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

    private void initLoyaltyRules() {
        if (loyaltyRuleRepository.count() == 0) {
            List<LoyaltyRule> rules = List.of(
                    new LoyaltyRule(null, "RESERVA_COMPLETADA", 10, null, true, true),
                    new LoyaltyRule(null, "COMPRA_SUPERIOR_50", 5, 50.0, true, false),
                    new LoyaltyRule(null, "COMPRA_SUPERIOR_100", 15, 100.0, true, false),
                    new LoyaltyRule(null, "CUMPLEANOS", 30, null, true, false),
                    new LoyaltyRule(null, "PRIMER_REGISTRO", 10, null, true, false),
                    new LoyaltyRule(null, "RESERVA_FRECUENTE", 10, null, true, true)
            );

            loyaltyRuleRepository.saveAll(rules);
            System.out.println(">>> Reglas de fidelización inicializadas");
        }
    }
}