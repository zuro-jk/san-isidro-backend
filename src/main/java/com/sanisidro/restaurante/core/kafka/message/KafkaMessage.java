package com.sanisidro.restaurante.core.kafka.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String topic;
    private String key;
    private String payload;
    private LocalDateTime timestamp;
}
