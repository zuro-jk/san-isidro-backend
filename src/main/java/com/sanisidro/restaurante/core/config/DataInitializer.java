package com.sanisidro.restaurante.core.config;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.customers.enums.LoyaltyRuleType;
import com.sanisidro.restaurante.features.customers.model.LoyaltyRule;
import com.sanisidro.restaurante.features.customers.repository.LoyaltyRuleRepository;
import com.sanisidro.restaurante.features.products.enums.MovementSource;
import com.sanisidro.restaurante.features.orders.model.OrderStatus;
import com.sanisidro.restaurante.features.orders.model.OrderStatusTranslation;
import com.sanisidro.restaurante.features.orders.model.OrderType;
import com.sanisidro.restaurante.features.orders.model.OrderTypeTranslation;
import com.sanisidro.restaurante.features.orders.repository.OrderStatusRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderTypeRepository;
import com.sanisidro.restaurante.features.products.enums.MovementType;
import com.sanisidro.restaurante.features.products.model.*;
import com.sanisidro.restaurante.features.products.repository.*;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderDetailRepository;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderRepository;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {


    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LoyaltyRuleRepository loyaltyRuleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TableRepository tableRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderTypeRepository orderTypeRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UnitRepository unitRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderDetailRepository purchaseOrderDetailRepository;


    @Override
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
        initLoyaltyRules();
        initTables();
        initOrderStatuses();
        initOrderTypes();
        initCategoriesAndProducts();
        initUnits();
        initIngredients();
        initProductIngredients();
        initInventories();
        initInventoryMovements();
        initSupplierUsers();
        initSuppliers();
        initPurchaseOrders();
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
                            .build()
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
                            .name("Mesa 2")
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
                            .name("Mesa 3")
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
                            .name("Mesa 4")
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
                            .name("Mesa 5")
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
                            .name("Mesa 6")
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
                            .name("Mesa 7")
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
                            .name("Mesa 8")
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
                            .name("Mesa 9")
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
                            .name("Mesa 10")
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
                            .name("Mesa 11")
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
                            .name("Mesa 12")
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
                            .name("Mesa 13")
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
                            .name("Mesa 14")
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
                            .name("Mesa 15")
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
                            .build()
            );
            tableRepository.saveAll(tables);
            System.out.println(">>> Mesas inicializadas");
        }
    }

    private void initOrderStatuses() {
        if (orderStatusRepository.count() > 0) {
            log.info(">>> Order statuses ya inicializados");
            return;
        }

        log.info(">>> Inicializando Order Statuses...");

        OrderStatus pending = OrderStatus.builder().code("PENDING").build();
        pending.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(pending).lang("en").name("Pending").description("Order has been placed but not processed yet").build(),
                OrderStatusTranslation.builder().orderStatus(pending).lang("es").name("Pendiente").description("El pedido ha sido realizado pero no procesado").build()
        ));

        OrderStatus inProgress = OrderStatus.builder().code("IN_PROGRESS").build();
        inProgress.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(inProgress).lang("en").name("In Progress").description("Order is being prepared").build(),
                OrderStatusTranslation.builder().orderStatus(inProgress).lang("es").name("En Proceso").description("El pedido está siendo preparado").build()
        ));

        OrderStatus readyForPickup = OrderStatus.builder().code("READY_FOR_PICKUP").build();
        readyForPickup.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(readyForPickup).lang("en").name("Ready for Pickup").description("Order is ready to be picked up").build(),
                OrderStatusTranslation.builder().orderStatus(readyForPickup).lang("es").name("Listo para recoger").description("El pedido está listo para ser recogido").build()
        ));

        OrderStatus completed = OrderStatus.builder().code("COMPLETED").build();
        completed.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(completed).lang("en").name("Completed").description("Order has been completed").build(),
                OrderStatusTranslation.builder().orderStatus(completed).lang("es").name("Completado").description("El pedido ha sido completado").build()
        ));

        OrderStatus cancelled = OrderStatus.builder().code("CANCELLED").build();
        cancelled.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(cancelled).lang("en").name("Cancelled").description("Order has been cancelled").build(),
                OrderStatusTranslation.builder().orderStatus(cancelled).lang("es").name("Cancelado").description("El pedido ha sido cancelado").build()
        ));

        OrderStatus failed = OrderStatus.builder().code("FAILED").build();
        failed.getTranslations().addAll(List.of(
                OrderStatusTranslation.builder().orderStatus(failed).lang("en").name("Failed").description("Order could not be processed").build(),
                OrderStatusTranslation.builder().orderStatus(failed).lang("es").name("Fallido").description("El pedido no pudo ser procesado").build()
        ));

        List<OrderStatus> statuses = List.of(pending, inProgress, readyForPickup, completed, cancelled, failed);
        orderStatusRepository.saveAll(statuses);

        statuses.forEach(s -> log.info(">>> OrderStatus '{}' inicializado con traducciones", s.getCode()));
    }

    private void initOrderTypes() {
        if (orderTypeRepository.count() > 0) {
            log.info(">>> Order types ya inicializados");
            return;
        }

        log.info(">>> Inicializando Order Types...");

        OrderType dineIn = OrderType.builder().code("DINE_IN").build();
        dineIn.getTranslations().addAll(List.of(
                OrderTypeTranslation.builder().orderType(dineIn).lang("en").name("Dine-in").description("Order for consumption inside the restaurant").build(),
                OrderTypeTranslation.builder().orderType(dineIn).lang("es").name("Presencial").description("Pedido para consumo dentro del restaurante").build()
        ));

        OrderType takeAway = OrderType.builder().code("TAKE_AWAY").build();
        takeAway.getTranslations().addAll(List.of(
                OrderTypeTranslation.builder().orderType(takeAway).lang("en").name("Take Away").description("Order to be picked up and taken away").build(),
                OrderTypeTranslation.builder().orderType(takeAway).lang("es").name("Para llevar").description("Pedido para recoger y llevar").build()
        ));

        OrderType delivery = OrderType.builder().code("DELIVERY").build();
        delivery.getTranslations().addAll(List.of(
                OrderTypeTranslation.builder().orderType(delivery).lang("en").name("Delivery").description("Order to be delivered to customer's address").build(),
                OrderTypeTranslation.builder().orderType(delivery).lang("es").name("Entrega a domicilio").description("Pedido a ser entregado en la dirección del cliente").build()
        ));

        List<OrderType> types = List.of(dineIn, takeAway, delivery);
        orderTypeRepository.saveAll(types);

        types.forEach(t -> log.info(">>> OrderType '{}' inicializado con traducciones", t.getCode()));
    }

    private void initCategoriesAndProducts() {
        if (categoryRepository.count() > 0) {
            log.info(">>> Categorías y productos ya inicializados");
            return;
        }

        log.info(">>> Inicializando categorías y productos...");

        // Crear categorías
        Category beverages = Category.builder().name("Bebidas").build();
        Category appetizers = Category.builder().name("Entradas").build();
        Category mainDishes = Category.builder().name("Platos principales").build();
        Category desserts = Category.builder().name("Postres").build();

        List<Category> categories = List.of(beverages, appetizers, mainDishes, desserts);
        categoryRepository.saveAll(categories);
        log.info(">>> Categorías inicializadas: Bebidas, Entradas, Platos principales, Postres");

        // Crear productos
        List<Product> products = List.of(
                Product.builder()
                        .name("Coca-Cola 500ml")
                        .price(new BigDecimal("3.50"))
                        .imageUrl("https://lacanga.com/cdn/shop/files/SLFk8fwFmHSQ7qcTv-sintitulo2556.png?v=1685580356")
                        .category(beverages)
                        .build(),
                Product.builder()
                        .name("Jugo de Naranja Natural")
                        .price(new BigDecimal("5.00"))
                        .imageUrl("https://image.tuasaude.com/media/article/go/jh/suco-de-laranja_67324.jpg")
                        .category(beverages)
                        .build(),
                Product.builder()
                        .name("Nachos con Queso")
                        .price(new BigDecimal("12.00"))
                        .imageUrl("https://www.divinacocina.es/wp-content/uploads/nachos-con-salsa-queso.jpg")
                        .category(appetizers)
                        .build(),
                Product.builder()
                        .name("Alitas BBQ")
                        .price(new BigDecimal("15.00"))
                        .imageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTR0GgvGRXZ9rGXssme3fuDO5SlUbf7tB8sOw&s")
                        .category(appetizers)
                        .build(),
                Product.builder()
                        .name("Hamburguesa Clásica")
                        .price(new BigDecimal("18.00"))
                        .imageUrl("https://tofuu.getjusto.com/orioneat-local/resized2/4Zg3b29e8fYXFT9ww-2400-x.webp")
                        .category(mainDishes)
                        .build(),
                Product.builder()
                        .name("Lomo Saltado")
                        .price(new BigDecimal("22.00"))
                        .imageUrl("https://origin.cronosmedia.glr.pe/large/2024/05/15/lg_664520c66ade8d4879400887.jpg")
                        .category(mainDishes)
                        .build(),
                Product.builder()
                        .name("Cheesecake")
                        .price(new BigDecimal("10.00"))
                        .imageUrl("https://www.recetasnestle.com.ec/sites/default/files/styles/recipe_detail_desktop_new/public/srh_recipes/7f9ebeaceea909a80306da27f0495c59.jpg?itok=_Xp6MoSe")
                        .category(desserts)
                        .build(),
                Product.builder()
                        .name("Brownie con Helado")
                        .price(new BigDecimal("12.00"))
                        .imageUrl("https://www.johaprato.com/files/brownie_y_helado.jpg")
                        .category(desserts)
                        .build()
        );

        productRepository.saveAll(products);
        log.info(">>> Productos inicializados correctamente");
    }

    private void initUnits() {
        if (unitRepository.count() > 0) {
            log.info(">>> Unidades ya inicializadas");
            return;
        }

        log.info(">>> Inicializando unidades...");

        List<Unit> units = List.of(
                Unit.builder().name("Gramo").symbol("g").build(),
                Unit.builder().name("Kilogramo").symbol("kg").build(),
                Unit.builder().name("Litro").symbol("l").build(),
                Unit.builder().name("Mililitro").symbol("ml").build(),
                Unit.builder().name("Unidad").symbol("u").build(),
                Unit.builder().name("Cucharada").symbol("tbsp").build(),
                Unit.builder().name("Cucharadita").symbol("tsp").build()
        );

        unitRepository.saveAll(units);
        log.info(">>> Unidades inicializadas correctamente");
    }

    private void initIngredients() {
        if (ingredientRepository.count() > 0) {
            log.info(">>> Ingredientes ya inicializados");
            return;
        }

        log.info(">>> Inicializando ingredientes...");

        Unit g = unitRepository.findBySymbol("g").orElseThrow();
        Unit u = unitRepository.findBySymbol("u").orElseThrow();
        Unit ml = unitRepository.findBySymbol("ml").orElseThrow();

        List<Ingredient> ingredients = List.of(
                Ingredient.builder().name("Pollo").unit(g).build(),
                Ingredient.builder().name("Papa").unit(g).build(),
                Ingredient.builder().name("Arroz").unit(g).build(),
                Ingredient.builder().name("Aceite").unit(ml).build(),
                Ingredient.builder().name("Pan").unit(u).build(),
                Ingredient.builder().name("Queso").unit(g).build(),
                Ingredient.builder().name("Carne de res").unit(g).build()
        );

        ingredientRepository.saveAll(ingredients);
        log.info(">>> Ingredientes inicializados correctamente");
    }

    private void initProductIngredients() {
        if (productIngredientRepository.count() > 0) {
            log.info(">>> ProductIngredients ya inicializados");
            return;
        }

        log.info(">>> Inicializando relaciones producto-ingredientes...");

        Product hamburguesa = productRepository.findByName("Hamburguesa Clásica").orElseThrow();
        Product lomoSaltado = productRepository.findByName("Lomo Saltado").orElseThrow();

        Ingredient carne = ingredientRepository.findByName("Carne de res").orElseThrow();
        Ingredient papa = ingredientRepository.findByName("Papa").orElseThrow();
        Ingredient arroz = ingredientRepository.findByName("Arroz").orElseThrow();
        Ingredient pan = ingredientRepository.findByName("Pan").orElseThrow();
        Ingredient queso = ingredientRepository.findByName("Queso").orElseThrow();

        List<ProductIngredient> relations = List.of(
                ProductIngredient.builder().product(hamburguesa).ingredient(carne).quantity(200.0).build(),
                ProductIngredient.builder().product(hamburguesa).ingredient(pan).quantity(1.0).build(),
                ProductIngredient.builder().product(hamburguesa).ingredient(queso).quantity(30.0).build(),

                ProductIngredient.builder().product(lomoSaltado).ingredient(carne).quantity(150.0).build(),
                ProductIngredient.builder().product(lomoSaltado).ingredient(papa).quantity(200.0).build(),
                ProductIngredient.builder().product(lomoSaltado).ingredient(arroz).quantity(150.0).build()
        );

        productIngredientRepository.saveAll(relations);
        log.info(">>> Relaciones producto-ingredientes inicializadas");
    }

    private void initInventories() {
        if (inventoryRepository.count() > 0) {
            log.info(">>> Inventarios ya inicializados");
            return;
        }

        log.info(">>> Inicializando inventarios...");

        List<Ingredient> ingredients = ingredientRepository.findAll();

        List<Inventory> inventories = ingredients.stream()
                .map(ing -> {
                    BigDecimal minStock = getDefaultMinStock(ing.getName());
                    BigDecimal initialStock = minStock.multiply(BigDecimal.valueOf(2));

                    return Inventory.builder()
                            .ingredient(ing)
                            .currentStock(initialStock)
                            .minimumStock(minStock)
                            .build();
                })
                .toList();

        inventoryRepository.saveAll(inventories);
        log.info(">>> Inventarios inicializados correctamente");
    }

    private BigDecimal getDefaultMinStock(String name) {
        return switch (name) {
            case "Pollo", "Aceite", "Carne de res" -> BigDecimal.valueOf(1000.0);
            case "Papa" -> BigDecimal.valueOf(2000.0);
            case "Arroz" -> BigDecimal.valueOf(3000.0);
            case "Pan" -> BigDecimal.valueOf(20.0);
            case "Queso" -> BigDecimal.valueOf(500.0);
            default -> BigDecimal.valueOf(100.0); // Para otros ingredientes, stock mínimo pequeño
        };
    }


    private void initInventoryMovements() {
        if (inventoryMovementRepository.count() > 0) {
            log.info(">>> Movimientos de inventario ya inicializados");
            return;
        }

        log.info(">>> Inicializando movimientos de inventario...");

        Ingredient pollo = ingredientRepository.findByName("Pollo").orElseThrow();
        Ingredient papa = ingredientRepository.findByName("Papa").orElseThrow();

        List<InventoryMovement> movements = List.of(
                InventoryMovement.builder()
                        .ingredient(pollo)
                        .type(MovementType.ENTRY)
                        .quantity(BigDecimal.valueOf(2000))
                        .reason("Compra inicial de pollo")
                        .source(MovementSource.PURCHASE)
                        .referenceId(null)
                        .build(),
                InventoryMovement.builder()
                        .ingredient(papa)
                        .type(MovementType.EXIT)
                        .quantity(BigDecimal.valueOf(500))
                        .reason("Consumo en pruebas de cocina")
                        .source(MovementSource.MANUAL)
                        .referenceId(null)
                        .build()
        );

        inventoryMovementRepository.saveAll(movements);
        log.info(">>> Movimientos de inventario inicializados");
    }

    private void initSupplierUsers() {
        Role supplierRole = roleRepository.findByName("ROLE_SUPPLIER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_SUPPLIER")));

        if (userRepository.findByEmail("sanfernado@gmail.com").isEmpty()) {
            User sanFernandoUser = User.builder()
                    .username("sanfernando_user")
                    .firstName("Carlos")
                    .lastName("Ramirez")
                    .email("sanfernado@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .roles(Set.of(supplierRole))
                    .enabled(true)
                    .build();
            userRepository.save(sanFernandoUser);
        }

        if (userRepository.findByEmail("gloria_user@gmail.com").isEmpty()) {
            User gloriaUser = User.builder()
                    .username("gloria_user")
                    .firstName("Ana")
                    .lastName("Torres")
                    .email("gloria_user@gmail.com")
                    .password(passwordEncoder.encode("password123"))
                    .roles(Set.of(supplierRole))
                    .enabled(true)
                    .build();
            userRepository.save(gloriaUser);
        }
    }

    private void initSuppliers() {
        User sanFernandoUser = userRepository.findByEmail("sanfernado@gmail.com").orElseThrow();
        User gloriaUser = userRepository.findByEmail("gloria_user@gmail.com").orElseThrow();

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