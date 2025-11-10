package com.sanisidro.restaurante.features.cashclosing.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.security.model.User;
import com.sanisidro.restaurante.features.cashclosing.dto.request.CashClosingSubmitRequest;
import com.sanisidro.restaurante.features.cashclosing.dto.response.CashClosingReportResponse;
import com.sanisidro.restaurante.features.cashclosing.dto.response.CashClosingSessionResponse;
import com.sanisidro.restaurante.features.cashclosing.models.CashClosingSession;
import com.sanisidro.restaurante.features.cashclosing.repository.CashClosingSessionRepository;
import com.sanisidro.restaurante.features.employees.model.Employee;
import com.sanisidro.restaurante.features.employees.repository.EmployeeRepository;
import com.sanisidro.restaurante.features.orders.enums.PaymentStatus;
import com.sanisidro.restaurante.features.orders.model.Payment;
import com.sanisidro.restaurante.features.orders.repository.PaymentRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashClosingService {

    private final CashClosingSessionRepository cashClosingRepository;
    private final EmployeeRepository employeeRepository;
    private final PaymentRepository paymentRepository;

    private static final BigDecimal DEFAULT_OPENING_BALANCE = new BigDecimal("100.00");

    /**
     * Lógica para OBTENER el reporte del sistema.
     * (Lo que el cajero ve ANTES de contar)
     */
    @Transactional(readOnly = true)
    public CashClosingReportResponse generateCurrentReport(User cashierUser) {

        Employee employee = getEmployeeFromUser(cashierUser);

        LocalDateTime shiftStartTime = cashClosingRepository
                .findFirstByEmployeeOrderByClosingTimeDesc(employee)
                .map(CashClosingSession::getClosingTime)
                .orElse(employee.getCreatedAt());

        List<Payment> payments = paymentRepository
                .findAllByOrder_EmployeeAndOrder_DateAfter(employee, shiftStartTime);

        Map<String, BigDecimal> salesByPaymentMethod = new HashMap<>();
        BigDecimal totalSales = BigDecimal.ZERO;

        for (Payment p : payments) {

            if (PaymentStatus.CONFIRMED.equals(p.getStatus())) {
                String methodName = p.getPaymentMethod().getCode();
                BigDecimal amount = p.getAmount();

                salesByPaymentMethod.merge(methodName, amount, BigDecimal::add);
                totalSales = totalSales.add(amount);
            }
        }

        BigDecimal cashSales = salesByPaymentMethod.getOrDefault("CASH", BigDecimal.ZERO);
        BigDecimal expectedCash = DEFAULT_OPENING_BALANCE.add(cashSales);

        return CashClosingReportResponse.builder()
                .cashierName(employee.getUser().getFullName())
                .shiftStartTime(shiftStartTime)
                .openingBalance(DEFAULT_OPENING_BALANCE)
                .salesByPaymentMethod(salesByPaymentMethod)
                .totalSales(totalSales)
                .expectedCashInDrawer(expectedCash)
                .build();
    }

    /**
     * Lógica para GUARDAR el cierre de caja.
     * (Lo que pasa cuando el cajero presiona "Confirmar Cierre")
     */
    @Transactional
    public void submitCashClosing(User cashierUser, CashClosingSubmitRequest request) {

        Employee employee = getEmployeeFromUser(cashierUser);

        CashClosingReportResponse systemReport = generateCurrentReport(cashierUser);

        BigDecimal countedCash = request.getCountedCashAmount();
        BigDecimal expectedCash = systemReport.getExpectedCashInDrawer();
        BigDecimal difference = countedCash.subtract(expectedCash);

        CashClosingSession session = CashClosingSession.builder()
                .employee(employee)
                .closingTime(LocalDateTime.now())
                .openingBalance(systemReport.getOpeningBalance())
                .salesByPaymentMethod(systemReport.getSalesByPaymentMethod())
                .expectedCash(expectedCash)
                .countedCash(countedCash)
                .difference(difference)
                .notes(request.getNotes())
                .build();

        cashClosingRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<CashClosingSessionResponse> getAllClosingSessions() {
        List<CashClosingSession> sessions = cashClosingRepository.findAllByOrderByClosingTimeDesc();

        return sessions.stream()
                .map(this::mapToSessionResponse)
                .collect(Collectors.toList());
    }

    // --- Método Utilidad ---
    private Employee getEmployeeFromUser(User user) {
        return employeeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró el perfil de empleado para el usuario: " + user.getUsername()));
    }

    private CashClosingSessionResponse mapToSessionResponse(CashClosingSession session) {
        return CashClosingSessionResponse.builder()
                .id(session.getId())
                .employeeName(session.getEmployee().getUser().getFullName())
                .closingTime(session.getClosingTime())
                .openingBalance(session.getOpeningBalance())
                .salesByPaymentMethod(session.getSalesByPaymentMethod())
                .expectedCash(session.getExpectedCash())
                .countedCash(session.getCountedCash())
                .difference(session.getDifference())
                .notes(session.getNotes())
                .build();
    }

}
