package com.sanisidro.restaurante.features.orders.dto.order.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private LocalDateTime date;

}
