package com.sanisidro.restaurante.features.customers.init;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.enums.LoyaltyRuleType;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class CustomerInitializer implements CommandLineRunner {

    private final LoyaltyRuleRepository loyaltyRuleRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initLoyaltyRules();
        initTestCustomers();
    }

    private void initLoyaltyRules() {
        if (loyaltyRuleRepository.count() == 0) {
            List<LoyaltyRule> rules = List.of(
                    LoyaltyRule.builder()
                            .name("Reserva completada")
                            .points(10)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(true)
                            .type(LoyaltyRuleType.RESERVATION)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Compra superior a 50")
                            .points(5)
                            .minPurchaseAmount(50.0)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.PURCHASE)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Compra superior a 100")
                            .points(15)
                            .minPurchaseAmount(100.0)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.PURCHASE)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Compra superior a 200")
                            .points(25)
                            .minPurchaseAmount(200.0)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.PURCHASE)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Cumpleaños")
                            .points(30)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.BIRTHDAY)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Primer registro")
                            .points(10)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.REFERRAL)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Reserva puntual")
                            .points(5)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(true)
                            .type(LoyaltyRuleType.RESERVATION)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Reserva anticipada")
                            .points(5)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(true)
                            .type(LoyaltyRuleType.RESERVATION)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Review dejada")
                            .points(10)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(true)
                            .type(LoyaltyRuleType.RESERVATION)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Visita en día festivo")
                            .points(15)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(true)
                            .type(LoyaltyRuleType.RESERVATION)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Pedido online")
                            .points(10)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.PURCHASE)
                            .build(),
                    LoyaltyRule.builder()
                            .name("Amigo referido hace compra")
                            .points(20)
                            .minPurchaseAmount(null)
                            .active(true)
                            .perPerson(false)
                            .type(LoyaltyRuleType.REFERRAL)
                            .build());

            loyaltyRuleRepository.saveAll(rules);
            System.out.println(">>> Reglas de fidelización inicializadas");
        }
    }

    private void initTestCustomers() {
        if (customerRepository.count() > 0) {
            log.info(">>> Clientes de prueba (entidad Customer) ya inicializados.");
            return;
        }
        log.info(">>> Creando entidades Customer para los usuarios de prueba...");

        User user1 = userRepository.findByUsername("cliente1")
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario de prueba 'cliente1' no encontrado. Asegúrate de que SecurityInitializer se ejecute primero."));
        User user2 = userRepository.findByUsername("cliente2")
                .orElseThrow(() -> new IllegalStateException("Usuario de prueba 'cliente2' no encontrado."));

        Customer customer1 = Customer.builder().user(user1).points(0).build();
        Customer customer2 = Customer.builder().user(user2).points(0).build();

        customerRepository.saveAll(List.of(customer1, customer2));
        log.info(">>> Entidades Customer para clientes de prueba creadas correctamente.");
    }

}
