package com.sanisidro.restaurante.features.cashclosing.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sanisidro.restaurante.features.cashclosing.models.CashClosingSession;
import com.sanisidro.restaurante.features.employees.model.Employee;

public interface CashClosingSessionRepository extends JpaRepository<CashClosingSession, Long> {
    /**
     * Esta es la función MÁS IMPORTANTE.
     * Busca la ÚLTIMA sesión de cierre de caja que hizo un empleado,
     * ordenada por la fecha de cierre más reciente.
     * * Así sabemos desde qué hora empezar a sumar las nuevas ventas.
     */
    Optional<CashClosingSession> findFirstByEmployeeOrderByClosingTimeDesc(Employee employee);

    /**
     * Busca todas las sesiones de cierre, ordenadas por la más reciente primero.
     */
    List<CashClosingSession> findAllByOrderByClosingTimeDesc();
}
