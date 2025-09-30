package com.sanisidro.restaurante.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.reservation")
public class ReservationProperties {
    private int bufferBeforeMinutes;
    private int bufferAfterMinutes;
}
