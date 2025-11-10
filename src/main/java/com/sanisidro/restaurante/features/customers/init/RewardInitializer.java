package com.sanisidro.restaurante.features.customers.init;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.features.feedbackloyalty.models.Reward;
import com.sanisidro.restaurante.features.feedbackloyalty.repository.RewardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(3)
public class RewardInitializer implements CommandLineRunner {

    private final RewardRepository rewardRepository;

    @Override
    public void run(String... args) throws Exception {
        initRewards();
    }

    private void initRewards() {
        if (rewardRepository.count() == 0) {
            List<Reward> rewards = List.of(
                    Reward.builder()
                            .name("Descuento del 10% en tu próxima compra")
                            .requiredPoints(50)
                            .description("Aplica para pedidos en línea o en restaurante.")
                            .active(true)
                            .build(),
                    Reward.builder()
                            .name("Postre gratis")
                            .requiredPoints(80)
                            .description("Canjea tus puntos por un postre gratuito en tu próxima visita.")
                            .active(true)
                            .build(),
                    Reward.builder()
                            .name("Bebida gratis")
                            .requiredPoints(40)
                            .description("Canjea una bebida de cortesía en tu siguiente pedido.")
                            .active(true)
                            .build(),
                    Reward.builder()
                            .name("Cupón de S/ 20")
                            .requiredPoints(120)
                            .description("Cupón válido para cualquier compra mayor a S/ 60.")
                            .active(true)
                            .build(),
                    Reward.builder()
                            .name("Cena para dos")
                            .requiredPoints(300)
                            .description("Incluye dos platos principales y una bebida por persona.")
                            .active(true)
                            .build());

            rewardRepository.saveAll(rewards);
            log.info(">>> Recompensas iniciales cargadas correctamente.");
        } else {
            log.info(">>> Las recompensas ya estaban inicializadas.");
        }
    }
}
