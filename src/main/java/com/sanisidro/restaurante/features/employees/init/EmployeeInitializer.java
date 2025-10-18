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
@Order(3)
public class EmployeeInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PositionRepository positionRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Map<String, Position> positions = initPositions();
        if (positions != null) {
            initEmployeesAndSchedules(positions);
        }
    }

    private Map<String, Position> initPositions() {
        if (positionRepository.count() > 0) {
            log.info(">>> Posiciones ya inicializadas.");
            return positionRepository.findAll().stream()
                    .collect(Collectors.toMap(Position::getName, p -> p));
        }
        log.info(">>> Inicializando Posiciones...");
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN no encontrado"));
        Role waiterRole = roleRepository.findByName("ROLE_WAITER")
                .orElseThrow(() -> new IllegalStateException("ROLE_WAITER no encontrado"));
        Role chefRole = roleRepository.findByName("ROLE_CHEF")
                .orElseThrow(() -> new IllegalStateException("ROLE_CHEF no encontrado"));
        Role cashierRole = roleRepository.findByName("ROLE_CASHIER")
                .orElseThrow(() -> new IllegalStateException("ROLE_CASHIER no encontrado"));

        Map<String, Position> positionsMap = Map.of(
                "ADMIN",
                Position.builder().name("ADMIN").description("Administrador general").roles(Set.of(adminRole)).build(),
                "MANAGER",
                Position.builder().name("MANAGER").description("Gerente del restaurante")
                        .roles(Set.of(adminRole, cashierRole)).build(), // Asumiendo que manager también puede ser admin
                "WAITER", Position.builder().name("WAITER").description("Mesero").roles(Set.of(waiterRole)).build(),
                "CHEF", Position.builder().name("CHEF").description("Cocinero").roles(Set.of(chefRole)).build(),
                "CASHIER", Position.builder().name("CASHIER").description("Cajero").roles(Set.of(cashierRole)).build());

        positionRepository.saveAll(positionsMap.values());
        log.info(">>> Posiciones inicializadas correctamente.");
        return positionsMap;
    }

    private void initEmployeesAndSchedules(Map<String, Position> positions) {
        if (employeeRepository.count() > 0) {
            log.info(">>> Empleados (Users y Employees) ya inicializados.");
            return;
        }
        log.info(">>> Creando Usuarios, Empleados y Horarios...");

        User adminUser = findUserAndSyncRoles("admin", positions.get("ADMIN"));
        User managerUser = findUserAndSyncRoles("jose_m", positions.get("MANAGER"));
        User waiterUser = findUserAndSyncRoles("maria_w", positions.get("WAITER"));
        User chefUser = findUserAndSyncRoles("carlos_c", positions.get("CHEF"));

        Employee adminEmployee = createEmployeeIfNotExists(adminUser, positions.get("ADMIN"), BigDecimal.valueOf(5000),
                LocalDate.now().minusMonths(6));
        Employee managerEmployee = createEmployeeIfNotExists(managerUser, positions.get("MANAGER"),
                BigDecimal.valueOf(4000), LocalDate.now().minusMonths(3));
        Employee waiterEmployee = createEmployeeIfNotExists(waiterUser, positions.get("WAITER"),
                BigDecimal.valueOf(2000), LocalDate.now().minusMonths(1));
        Employee chefEmployee = createEmployeeIfNotExists(chefUser, positions.get("CHEF"), BigDecimal.valueOf(3000),
                LocalDate.now().minusMonths(2));
        log.info(">>> Entidades Employee creadas o verificadas.");

        if (scheduleRepository.count() > 0) {
            log.info(">>> Horarios ya inicializados.");
        } else {
            log.info(">>> Creando Horarios...");
            scheduleRepository.saveAll(List.of(
                    Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),
                    Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(), // Admin trabaja L-V
                    Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),
                    Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),
                    Schedule.builder().employee(adminEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                            .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),

                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.MONDAY)
                            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(), // Manager L-S
                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                            .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                    Schedule.builder().employee(managerEmployee).dayOfWeek(DayOfWeekEnum.SATURDAY)
                            .startTime(LocalTime.of(12, 0)).endTime(LocalTime.of(20, 0)).build(),

                    Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                            .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(22, 0)).build(), // Mesero Miér-Dom
                    Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                            .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(22, 0)).build(),
                    Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                            .startTime(LocalTime.of(16, 0)).endTime(LocalTime.of(0, 0)).build(), // Turno noche
                    Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.SATURDAY)
                            .startTime(LocalTime.of(16, 0)).endTime(LocalTime.of(0, 0)).build(),
                    Schedule.builder().employee(waiterEmployee).dayOfWeek(DayOfWeekEnum.SUNDAY)
                            .startTime(LocalTime.of(12, 0)).endTime(LocalTime.of(20, 0)).build(),

                    Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.TUESDAY)
                            .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(19, 0)).build(), // Chef Mar-Sáb
                    Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.WEDNESDAY)
                            .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(19, 0)).build(),
                    Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.THURSDAY)
                            .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(19, 0)).build(),
                    Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.FRIDAY)
                            .startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(19, 0)).build(),
                    Schedule.builder().employee(chefEmployee).dayOfWeek(DayOfWeekEnum.SATURDAY)
                            .startTime(LocalTime.of(12, 0)).endTime(LocalTime.of(20, 0)).build()));
            log.info(">>> Horarios inicializados correctamente.");
        }
    }

    private User findUserAndSyncRoles(String username, Position position) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario base para empleado '" + username
                        + "' no encontrado. Asegúrate de que SecurityInitializer se ejecute primero."));
        user.syncRolesWithPosition(position);
        return userRepository.save(user);
    }

    private Employee createEmployeeIfNotExists(User user, Position position, BigDecimal salary, LocalDate hireDate) {
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
