package com.sanisidro.restaurante.features.restaurant.init;

import java.time.LocalTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.Store;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.StoreRepository;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(7)
public class RestaurantInitializer implements CommandLineRunner {

        private final TableRepository tableRepository;
        private final StoreRepository storeRepository;

        @Override
        public void run(String... args) throws Exception {
                initTables();
                initStores();
        }

        private void initTables() {
                if (tableRepository.count() == 0) {
                        List<TableEntity> tables = List.of(
                                        TableEntity.builder()
                                                        .code("A1")
                                                        .alias("Terraza - ventana")
                                                        .capacity(2)
                                                        .minCapacity(2)
                                                        .optimalCapacity(2)
                                                        .priority(1)
                                                        .description("Mesa pequeña cerca de la ventana")
                                                        .openTime(LocalTime.of(8, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(60)
                                                        .bufferBeforeMinutes(5)
                                                        .bufferAfterMinutes(5)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("A2")
                                                        .alias("Terraza - central")
                                                        .capacity(2)
                                                        .minCapacity(1)
                                                        .optimalCapacity(2)
                                                        .priority(2)
                                                        .description("Mesa pequeña junto a la barra")
                                                        .openTime(LocalTime.of(8, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(60)
                                                        .bufferBeforeMinutes(5)
                                                        .bufferAfterMinutes(5)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("A3")
                                                        .alias("Terraza - esquina")
                                                        .capacity(4)
                                                        .minCapacity(2)
                                                        .optimalCapacity(4)
                                                        .priority(1)
                                                        .description("Mesa mediana en zona central")
                                                        .openTime(LocalTime.of(12, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(90)
                                                        .bufferBeforeMinutes(10)
                                                        .bufferAfterMinutes(10)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("B1")
                                                        .alias("Salón central - pareja")
                                                        .capacity(4)
                                                        .minCapacity(2)
                                                        .optimalCapacity(4)
                                                        .priority(2)
                                                        .description("Mesa mediana junto a la pared")
                                                        .openTime(LocalTime.of(12, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(90)
                                                        .bufferBeforeMinutes(10)
                                                        .bufferAfterMinutes(10)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("B2")
                                                        .alias("Salón central - familiar")
                                                        .capacity(4)
                                                        .minCapacity(2)
                                                        .optimalCapacity(4)
                                                        .priority(3)
                                                        .description("Mesa mediana zona VIP")
                                                        .openTime(LocalTime.of(12, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(90)
                                                        .bufferBeforeMinutes(10)
                                                        .bufferAfterMinutes(10)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("B3")
                                                        .alias("Salón central - barra cercana")
                                                        .capacity(6)
                                                        .minCapacity(4)
                                                        .optimalCapacity(6)
                                                        .priority(1)
                                                        .description("Mesa grande familiar")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(120)
                                                        .bufferBeforeMinutes(15)
                                                        .bufferAfterMinutes(15)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("C1")
                                                        .alias("VIP - mesa chica")
                                                        .capacity(6)
                                                        .minCapacity(4)
                                                        .optimalCapacity(6)
                                                        .priority(2)
                                                        .description("Mesa grande junto al jardín")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(120)
                                                        .bufferBeforeMinutes(15)
                                                        .bufferAfterMinutes(15)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("C2")
                                                        .alias("VIP - mesa mediana")
                                                        .capacity(6)
                                                        .minCapacity(4)
                                                        .optimalCapacity(6)
                                                        .priority(3)
                                                        .description("Mesa grande esquina")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(120)
                                                        .bufferBeforeMinutes(15)
                                                        .bufferAfterMinutes(15)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("C3")
                                                        .alias("VIP - mesa grande")
                                                        .capacity(8)
                                                        .minCapacity(6)
                                                        .optimalCapacity(8)
                                                        .priority(1)
                                                        .description("Mesa extra grande para grupos")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(150)
                                                        .bufferBeforeMinutes(20)
                                                        .bufferAfterMinutes(20)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("D1")
                                                        .alias("Jardín - bajo la pérgola")
                                                        .capacity(8)
                                                        .minCapacity(6)
                                                        .optimalCapacity(8)
                                                        .priority(2)
                                                        .description("Mesa extra grande zona VIP")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(150)
                                                        .bufferBeforeMinutes(20)
                                                        .bufferAfterMinutes(20)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("D2")
                                                        .alias("Jardín - junto a la fuente")
                                                        .capacity(2)
                                                        .minCapacity(1)
                                                        .optimalCapacity(2)
                                                        .priority(3)
                                                        .description("Mesa pequeña cerca del baño")
                                                        .openTime(LocalTime.of(8, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(60)
                                                        .bufferBeforeMinutes(5)
                                                        .bufferAfterMinutes(5)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("D3")
                                                        .alias("Jardín - esquina familiar")
                                                        .capacity(4)
                                                        .minCapacity(2)
                                                        .optimalCapacity(4)
                                                        .priority(4)
                                                        .description("Mesa mediana interior")
                                                        .openTime(LocalTime.of(12, 0))
                                                        .closeTime(LocalTime.of(22, 0))
                                                        .reservationDurationMinutes(90)
                                                        .bufferBeforeMinutes(10)
                                                        .bufferAfterMinutes(10)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("E1")
                                                        .alias("Privado - mesa ejecutiva")
                                                        .capacity(6)
                                                        .minCapacity(4)
                                                        .optimalCapacity(6)
                                                        .priority(4)
                                                        .description("Mesa grande exterior")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(120)
                                                        .bufferBeforeMinutes(15)
                                                        .bufferAfterMinutes(15)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("E2")
                                                        .alias("Privado - reunión de grupo")
                                                        .capacity(8)
                                                        .minCapacity(6)
                                                        .optimalCapacity(8)
                                                        .priority(3)
                                                        .description("Mesa para celebraciones")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(150)
                                                        .bufferBeforeMinutes(20)
                                                        .bufferAfterMinutes(20)
                                                        .status(TableStatus.FREE)
                                                        .build(),
                                        TableEntity.builder()
                                                        .code("E3")
                                                        .alias("Privado - celebración especial")
                                                        .capacity(10)
                                                        .minCapacity(8)
                                                        .optimalCapacity(10)
                                                        .priority(1)
                                                        .description("Mesa extra grande eventos")
                                                        .openTime(LocalTime.of(18, 0))
                                                        .closeTime(LocalTime.of(23, 0))
                                                        .reservationDurationMinutes(180)
                                                        .bufferBeforeMinutes(25)
                                                        .bufferAfterMinutes(25)
                                                        .status(TableStatus.FREE)
                                                        .build());
                        tableRepository.saveAll(tables);
                        System.out.println(">>> Mesas inicializadas");
                }
        }

        private void initStores() {
                if (storeRepository.count() > 0) {
                        log.info(">>> Sucursales ya inicializadas.");
                        return;
                }
                log.info(">>> Inicializando sucursal principal...");
                Store mainStore = Store.builder()
                                .name("Sede Principal Ica")
                                .address("Av San Martin 1149, Ica 11001")
                                .openTime(LocalTime.NOON)
                                .closeTime(LocalTime.MIDNIGHT)
                                .phone("930532846")
                                .latitude(-14.0729952)
                                .longitude(-75.7275384)
                                .build();
                storeRepository.save(mainStore);
                log.info(">>> Sucursal principal inicializada correctamente con ID: {}", mainStore.getId());
        }
}