package com.sanisidro.restaurante.features.customers.scheduler;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sanisidro.restaurante.features.customers.enums.ReservationStatus;
import com.sanisidro.restaurante.features.customers.model.Reservation;
import com.sanisidro.restaurante.features.customers.repository.ReservationRepository;
import com.sanisidro.restaurante.features.restaurant.enums.TableStatus;
import com.sanisidro.restaurante.features.restaurant.model.TableEntity;
import com.sanisidro.restaurante.features.restaurant.repository.TableRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;

    private static final ZoneId RESTAURANT_ZONE = ZoneId.of("America/Lima");

    /**
     * Cada 5 minutos verifica reservas no-show
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void releaseNoShowTables() {
        ZonedDateTime now = ZonedDateTime.now(RESTAURANT_ZONE);

        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndReservationDateOrderByReservationTimeAsc(
                        ReservationStatus.CONFIRMED,
                        now.toLocalDate());

        for (Reservation r : expiredReservations) {
            LocalTime reservationTime = r.getReservationTime();
            TableEntity table = r.getTable();

            if (reservationTime.plusMinutes(15).isBefore(now.toLocalTime())) {
                r.setStatus(com.sanisidro.restaurante.features.customers.enums.ReservationStatus.CANCELLED);
                reservationRepository.save(r);

                // Liberar mesa
                table.setStatus(TableStatus.FREE);
                tableRepository.save(table);

                log.info("⚠️ Reserva no-show cancelada automáticamente: id={}, mesa={}, hora={}",
                        r.getId(), table.getCode(), r.getReservationTime());
            }
        }
    }
}
