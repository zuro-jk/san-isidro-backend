package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
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
    private final TableRepository tableRepository;

    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
        initLoyaltyRules();
        initTables();
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

    private void initTables() {
        if (tableRepository.count() == 0) {
            List<TableEntity> tables = List.of(
                    TableEntity.builder()
                            .name("Mesa 1")
                            .capacity(2)
                            .description("Mesa pequeña cerca de la ventana")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(60)
                            .bufferBeforeMinutes(5)
                            .bufferAfterMinutes(5)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 2")
                            .capacity(2)
                            .description("Mesa pequeña junto a la barra")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(60)
                            .bufferBeforeMinutes(5)
                            .bufferAfterMinutes(5)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 3")
                            .capacity(4)
                            .description("Mesa mediana en zona central")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(90)
                            .bufferBeforeMinutes(10)
                            .bufferAfterMinutes(10)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 4")
                            .capacity(4)
                            .description("Mesa mediana junto a la pared")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(90)
                            .bufferBeforeMinutes(10)
                            .bufferAfterMinutes(10)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 5")
                            .capacity(4)
                            .description("Mesa mediana zona VIP")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(90)
                            .bufferBeforeMinutes(10)
                            .bufferAfterMinutes(10)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 6")
                            .capacity(6)
                            .description("Mesa grande familiar")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(120)
                            .bufferBeforeMinutes(15)
                            .bufferAfterMinutes(15)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 7")
                            .capacity(6)
                            .description("Mesa grande junto al jardín")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(120)
                            .bufferBeforeMinutes(15)
                            .bufferAfterMinutes(15)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 8")
                            .capacity(6)
                            .description("Mesa grande esquina")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(120)
                            .bufferBeforeMinutes(15)
                            .bufferAfterMinutes(15)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 9")
                            .capacity(8)
                            .description("Mesa extra grande para grupos")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(150)
                            .bufferBeforeMinutes(20)
                            .bufferAfterMinutes(20)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 10")
                            .capacity(8)
                            .description("Mesa extra grande zona VIP")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(150)
                            .bufferBeforeMinutes(20)
                            .bufferAfterMinutes(20)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 11")
                            .capacity(2)
                            .description("Mesa pequeña cerca del baño")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(60)
                            .bufferBeforeMinutes(5)
                            .bufferAfterMinutes(5)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 12")
                            .capacity(4)
                            .description("Mesa mediana interior")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(90)
                            .bufferBeforeMinutes(10)
                            .bufferAfterMinutes(10)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 13")
                            .capacity(6)
                            .description("Mesa grande exterior")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(120)
                            .bufferBeforeMinutes(15)
                            .bufferAfterMinutes(15)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 14")
                            .capacity(8)
                            .description("Mesa para celebraciones")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(150)
                            .bufferBeforeMinutes(20)
                            .bufferAfterMinutes(20)
                            .status(TableStatus.FREE)
                            .build(),
                    TableEntity.builder()
                            .name("Mesa 15")
                            .capacity(10)
                            .description("Mesa extra grande eventos")
                            .openTime(LocalTime.of(12, 0))
                            .closeTime(LocalTime.of(22, 0))
                            .reservationDurationMinutes(180)
                            .bufferBeforeMinutes(25)
                            .bufferAfterMinutes(25)
                            .status(TableStatus.FREE)
                            .build()
            );

            tableRepository.saveAll(tables);
            System.out.println(">>> Mesas inicializadas");
        }
    }

}