package com.sanisidro.restaurante.features.orders.dto.order.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.sanisidro.restaurante.features.orders.dto.orderdetail.response.OrderDetailInOrderResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long employeeId;
    private String employeeName;

    private String deliveryStreet;
    private String deliveryReference;
    private String deliveryCity;
    private String deliveryInstructions;
    private String deliveryProvince;
    private String deliveryZipCode;
    private Double deliveryLatitude;
    private Double deliveryLongitude;

    private Long tableId;
    private String tableCode;

    private Long pickupStoreId;
    private String pickupStoreName;
    private String pickupStoreAddress;

    private Integer estimatedTime;
    private String estimatedDistance;
    private String estimatedDuration;

    private Double currentLatitude;
    private Double currentLongitude;

    private LocalDateTime date;

    private Long statusId;
    private String statusName;
    private Long typeId;
    private String typeName;

    private BigDecimal total;
    
    private List<OrderDetailInOrderResponse> details;

}
