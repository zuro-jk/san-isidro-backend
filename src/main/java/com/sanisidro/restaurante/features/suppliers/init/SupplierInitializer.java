package com.sanisidro.restaurante.features.suppliers.init;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.repository.IngredientRepository;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderRepository;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(6)
public class SupplierInitializer implements CommandLineRunner {

    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (supplierRepository.count() == 0) {
            initSuppliers();
        }
        if (purchaseOrderRepository.count() == 0) {
            initPurchaseOrders();
        }
    }

    private void initSuppliers() {
        log.info(">>> Creando entidades Supplier...");
        User sanFernandoUser = userRepository.findByEmail("sanfernando@example.com")
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario 'sanfernando@example.com' no encontrado. Asegúrate de que SecurityInitializer se ejecute primero."));
        User gloriaUser = userRepository.findByEmail("gloria@example.com")
                .orElseThrow(() -> new IllegalStateException("Usuario 'gloria@example.com' no encontrado."));

        if (supplierRepository.findByCompanyName("San Fernando").isEmpty()) {
            Supplier sanFernandoSupplier = Supplier.builder()
                    .companyName("San Fernando")
                    .contactName("Carlos Ramirez")
                    .phone("999111222")
                    .address("Av. Industrial 123, Lima")
                    .user(sanFernandoUser)
                    .build();
            supplierRepository.save(sanFernandoSupplier);
        }

        if (supplierRepository.findByCompanyName("Gloria").isEmpty()) {
            Supplier gloriaSupplier = Supplier.builder()
                    .companyName("Gloria")
                    .contactName("Ana Torres")
                    .phone("999333444")
                    .address("Av. Colonial 456, Callao")
                    .user(gloriaUser)
                    .build();
            supplierRepository.save(gloriaSupplier);
        }
        log.info(">>> Entidades Supplier creadas correctamente.");
    }

    private void initPurchaseOrders() {
        if (purchaseOrderRepository.count() > 0) {
            log.info(">>> Órdenes de compra ya inicializadas");
            return;
        }

        log.info(">>> Inicializando órdenes de compra...");

        Supplier sanFernando = supplierRepository.findByCompanyName("San Fernando")
                .orElseThrow();
        Supplier gloria = supplierRepository.findByCompanyName("Gloria")
                .orElseThrow();

        Ingredient pollo = ingredientRepository.findByName("Pollo").orElseThrow();
        Ingredient papa = ingredientRepository.findByName("Papa").orElseThrow();
        Ingredient arroz = ingredientRepository.findByName("Arroz").orElseThrow();

        PurchaseOrder order1 = PurchaseOrder.builder()
                .supplier(sanFernando)
                .date(java.time.LocalDateTime.now().minusDays(7))
                .status(PurchaseOrderStatus.RECEIVED)
                .total(BigDecimal.valueOf(5000))
                .build();

        PurchaseOrderDetail detail1 = PurchaseOrderDetail.builder()
                .order(order1)
                .ingredient(pollo)
                .quantity(1000)
                .unitPrice(BigDecimal.valueOf(10.0))
                .build();

        PurchaseOrderDetail detail2 = PurchaseOrderDetail.builder()
                .order(order1)
                .ingredient(papa)
                .quantity(500)
                .unitPrice(BigDecimal.valueOf(2.0))
                .build();

        order1.replaceDetails(Set.of(detail1, detail2));
        purchaseOrderRepository.save(order1);

        PurchaseOrder order2 = PurchaseOrder.builder()
                .supplier(gloria)
                .date(java.time.LocalDateTime.now().minusDays(3))
                .status(PurchaseOrderStatus.PENDING)
                .total(BigDecimal.valueOf(2000))
                .build();

        PurchaseOrderDetail detail3 = PurchaseOrderDetail.builder()
                .order(order2)
                .ingredient(arroz)
                .quantity(800)
                .unitPrice(BigDecimal.valueOf(3.5))
                .build();

        order2.replaceDetails(Set.of(detail3));
        purchaseOrderRepository.save(order2);

        log.info(">>> Órdenes de compra inicializadas correctamente");
    }

}
