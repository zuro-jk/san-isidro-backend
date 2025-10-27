package com.sanisidro.restaurante.features.dashboard.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.sanisidro.restaurante.features.customers.dto.reservation.response.ReservationResponse;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryResponse {
    private int ordersToday;
    private BigDecimal salesToday;
    private int reservationsToday;
    private List<InventoryResponse> lowStock;
    private List<ReservationResponse> upcomingReservations;
    private List<ReviewResponse> recentReviews;
    private int satisfaction;

    private Map<LocalDate, Integer> ordersWeek; // Órdenes por día
    private Map<LocalDate, BigDecimal> salesWeek; // Ventas por día
    private Map<LocalDate, Integer> reservationsWeek; // Reservaciones por día
}
