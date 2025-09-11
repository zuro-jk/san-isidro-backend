package com.sanisidro.restaurante.features.notifications.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationMetricsService {

    private final MeterRegistry meterRegistry;

    public void incrementSent(String type) {
        Counter.builder("notifications.sent")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }

    public void incrementFailed(String type, String reason) {
        Counter.builder("notifications.failed")
                .tag("type", type)
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

}
