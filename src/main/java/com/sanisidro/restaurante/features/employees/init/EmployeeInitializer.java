package com.sanisidro.restaurante.features.employees.init;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.security.model.Role;
import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.core.security.repository.RoleRepository;
import com.sanisidro.restaurante.core.security.repository.UserRepository;
import com.sanisidro.restaurante.features.employees.enums.DayOfWeekEnum;
import com.sanisidro.restaurante.features.employees.enums.EmploymentStatus;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.model.Position;
import com.sanisidro.restaurante.features.employees.model.Schedule;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.employees.repository.PositionRepository;
import com.sanisidro.restaurante.features.employees.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
@Order(4)
public class EmployeeInitializer implements CommandLineRunner {

        private final EmployeeRepository employeeRepository;
        private final PositionRepository positionRepository;
        private final ScheduleRepository scheduleRepository;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        // --- ¡¡¡LA CORRECCIÓN!!! ---
        private static final LocalTime END_OF_DAY = LocalTime.of(23, 59, 59);

        @Override
        @Transactional
        public void run(String... args) throws Exception {
                log.warn("******************************************************");
                log.warn("*** EJECUTANDO EmployeeInitializer (Modo DEV)      ***");
                log.warn("******************************************************");

                Map<String, Position> positions = initPositions();
                if (positions != null) {
                        initEmployeesAndSchedules(positions);
                }
        }

        private Map<String, Position> initPositions() {
                // ... (este método está bien, no se necesita cambiar) ...
                if (positionRepository.count() > 0) {
                        log.info(">>> Posiciones ya inicializadas.");
                        return positionRepository.findAll().stream()
                                        .collect(Collectors.toMap(Position::getName, p -> p));
                }
                log.info(">>> Inicializando Posiciones...");
                Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no encontrado"));
                Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                                .orElseThrow(() -> new IllegalStateException("ROLE_MANAGER no encontrado"));
                Role waiterRole = roleRepository.findByName("ROLE_WAITER")
                                .orElseThrow(() -> new IllegalStateException("ROLE_WAITER no encontrado"));
                Role chefRole = roleRepository.findByName("ROLE_CHEF")
                                .orElseThrow(() -> new IllegalStateException("ROLE_CHEF no encontrado"));
                Role cashierRole = roleRepository.findByName("ROLE_CASHIER")
                                .orElseThrow(() -> new IllegalStateException("ROLE_CASHIER no encontrado"));

                Map<String, Position> positionsMap = Map.of(
                                "ADMIN",
                                Position.builder().name("ADMIN").description("Administrador general")
                                                .roles(Set.of(adminRole)).build(),
                                "MANAGER",
                                Position.builder().name("MANAGER").description("Gerente del restaurante")
                                                // --- AQUÍ ESTÁ EL CAMBIO ---
                                                .roles(Set.of(cashierRole, waiterRole, managerRole))
                                                .build(),
                                "WAITER",
                                Position.builder().name("WAITER").description("Mesero").roles(Set.of(waiterRole))
                                                .build(),
                                "CHEF",
                                Position.builder().name("CHEF").description("Cocinero").roles(Set.of(chefRole)).build(),
                                "CASHIER", Position.builder().name("CASHIER").description("Cajero")
                                                .roles(Set.of(cashierRole)).build());

                positionRepository.saveAll(positionsMap.values());
                log.info(">>> Posiciones inicializadas correctamente.");
                return positionsMap;
        }

        private void initEmployeesAndSchedules(Map<String, Position> positions) {
                log.info(">>> Sincronizando Empleados y Horarios (Modo DEV)...");

                Employee adminEmployee = findAndCreateEmployee("admin", positions.get("ADMIN"),
                                BigDecimal.valueOf(5000), LocalDate.now().minusMonths(6));
                Employee managerEmployee = findAndCreateEmployee("jose_m", positions.get("MANAGER"),
                                BigDecimal.valueOf(4000), LocalDate.now().minusMonths(3));
                Employee waiterEmployee = findAndCreateEmployee("maria_w", positions.get("WAITER"),
                                BigDecimal.valueOf(2000), LocalDate.now().minusMonths(1));
                Employee chefEmployee = findAndCreateEmployee("carlos_c", positions.get("CHEF"),
                                BigDecimal.valueOf(3000), LocalDate.now().minusMonths(2));
                log.info(">>> Entidades Employee creadas o verificadas.");

                log.info(">>> [IMPORTANTE] Borrando TODOS los horarios antiguos de la BD...");
                scheduleRepository.deleteAll();
                scheduleRepository.flush();

                log.info(">>> Creando horarios nuevos con la hora de fin: {}", END_OF_DAY);

                // AHORA SE USA END_OF_DAY (23:59:59) EN LUGAR DE LocalTime.MAX
                scheduleRepository.saveAll(List.of(
                                // Admin
                                Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),

                                // Manager (jose_m)
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.SATURDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                // Waiter (maria_w) - Lunes a Viernes
                                Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),

                                // Chef (carlos_c) - Lunes a Viernes
                                Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build(),
                                Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                                                .startTime(LocalTime.of(12, 0)).endTime(END_OF_DAY).build()));
                log.info(">>> Horarios de empleados sincronizados correctamente.");
        }

        private Employee findAndCreateEmployee(String username, Position position, BigDecimal salary,
                        LocalDate hireDate) {
                // ... (este método está bien, no se necesita cambiar) ...
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new IllegalStateException("Usuario base para empleado '" + username
                                                + "' no encontrado. Asegúrate de que SecurityInitializer (Order 1) se ejecute primero."));
                user.syncRolesWithPosition(position);
                userRepository.save(user);

                return employeeRepository.findByUserId(user.getId()).orElseGet(() -> {
                        log.debug("Creando entidad Employee para usuario: {}", user.getUsername());
                        Employee newEmployee = Employee.builder()
                                        .user(user)
                                        .position(position)
                                        .salary(salary)
                                        .hireDate(hireDate)
                                        .status(EmploymentStatus.ACTIVE)
                                        .build();
                        return employeeRepository.save(newEmployee);
                });
        }
}