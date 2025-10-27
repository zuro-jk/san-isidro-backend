package com.sanisidro.restaurante.features.dashboard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sanisidro.restaurante.features.customers.dto.reservation.response.ReservationResponse;
import com.sanisidro.restaurante.features.customers.dto.review.response.ReviewResponse;
import com.sanisidro.restaurante.features.customers.service.ReservationService;
import com.sanisidro.restaurante.features.customers.service.ReviewService;
import com.sanisidro.restaurante.features.dashboard.dto.response.DashboardSummaryResponse;
import com.sanisidro.restaurante.features.orders.service.OrderService;
import com.sanisidro.restaurante.features.products.dto.inventory.response.InventoryResponse;
import com.sanisidro.restaurante.features.products.service.InventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderService orderService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;
    private final InventoryService inventoryService;

    public DashboardSummaryResponse getDashboardSummary() {

        LocalDate today = LocalDate.now();

        int ordersToday = orderService.countOrdersByDate(today);
        BigDecimal salesToday = orderService.calculateSalesByDate(today);
        int reservationsToday = reservationService.countReservationsByDate(today);

        List<InventoryResponse> lowStock = inventoryService.findLowStockInventories();
        List<ReservationResponse> upcomingReservations = reservationService.findUpcomingReservations();
        List<ReviewResponse> recentReviews = reviewService.findRecentReviews(5);

        int satisfaction = reviewService.calculateAverageSatisfaction();

        Map<LocalDate, Integer> ordersWeek = orderService.countOrdersLast7Days();
        Map<LocalDate, BigDecimal> salesWeek = orderService.calculateSalesLast7Days();
        Map<LocalDate, Integer> reservationsWeek = reservationService.countReservationsNext7Days();

        return new DashboardSummaryResponse(
                ordersToday,
                salesToday,
                reservationsToday,
                lowStock,
                upcomingReservations,
                recentReviews,
                satisfaction,
                ordersWeek,
                salesWeek,
                reservationsWeek);
    }

}
