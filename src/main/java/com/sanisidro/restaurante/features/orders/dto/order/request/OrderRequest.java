package com.sanisidro.restaurante.features.orders.dto.order.request;

import java.util.List;

import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.request.OrderDetailInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequest {

    private Long customerId;
    private Long employeeId;

    @Valid
    private DeliveryAddressRequest deliveryAddress;

    private Long tableId;
    private Long pickupStoreId;

    @NotNull(message = "El estado de la orden es obligatorio")
    private Long statusId;

    @NotNull(message = "El tipo de orden es obligatorio")
    private Long typeId;

    @NotEmpty(message = "La orden debe contener al menos un detalle")
    @Valid
    private List<@Valid OrderDetailInOrderRequest> details;

    @Valid
    private List<@Valid PaymentInOrderRequest> payments;

    @Valid
    private List<@Valid DocumentInOrderRequest> documents;

}
