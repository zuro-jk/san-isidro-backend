package com.sanisidro.restaurante.features.orders.dto.order.request;

import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentRequest;
import com.sanisidro.restaurante.features.orders.dto.orderdetail.request.OrderDetailInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.payment.request.PaymentInOrderRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    private Long customerId;
    private Long employeeId;

    @Valid
    private DeliveryAddressRequest deliveryAddress;

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
