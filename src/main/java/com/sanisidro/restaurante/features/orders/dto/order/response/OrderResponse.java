package com.sanisidro.restaurante.features.orders.dto.order.response;

import com.sanisidro.restaurante.features.orders.dto.orderdetail.response.OrderDetailInOrderResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long employeeId;
    private String employeeName;
    private Long addressId;
    private String addressDescription;
    private LocalDateTime date;
    private Long statusId;
    private String statusName;
    private Long typeId;
    private String typeName;
    private BigDecimal total;
    private List<OrderDetailInOrderResponse> details;

}
