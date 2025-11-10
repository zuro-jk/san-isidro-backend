package com.sanisidro.restaurante.features.feedbackloyalty.service;

import com.sanisidro.restaurante.core.exceptions.InvalidPointsOperationException;
import com.sanisidro.restaurante.features.customers.enums.PointHistoryEventType;
import com.sanisidro.restaurante.features.customers.model.Customer;
import com.sanisidro.restaurante.features.customers.repository.CustomerRepository;
import com.sanisidro.restaurante.features.feedbackloyalty.models.PointsHistory;
import com.sanisidro.restaurante.features.feedbackloyalty.repository.PointsHistoryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointsHistoryService {

    private final CustomerRepository customerRepository;
    private final PointsHistoryRepository pointsHistoryRepository;

    /**
     * Aplica puntos a un cliente y guarda el historial.
     */
    @Transactional
    public int applyPoints(Customer customer, int points, PointHistoryEventType eventType) {
        if (points <= 0) {
            throw new InvalidPointsOperationException("No se pueden aplicar puntos <= 0");
        }

        customer.setPoints(customer.getPoints() + points);
        customerRepository.save(customer);

        PointsHistory history = PointsHistory.builder()
                .customer(customer)
                .points(points)
                .event(eventType)
                .build();
        pointsHistoryRepository.save(history);

        return points;
    }

    /**
     * Obtiene el historial de puntos de un cliente paginado.
     */
    @Transactional(readOnly = true)
    public Page<PointsHistory> getHistoryByCustomer(Customer customer, Pageable pageable) {
        return pointsHistoryRepository.findByCustomerId(customer.getId(), pageable);
    }

}
