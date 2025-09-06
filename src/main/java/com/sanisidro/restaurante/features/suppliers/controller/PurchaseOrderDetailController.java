package com.sanisidro.restaurante.features.suppliers.controller;

import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.request.PurchaseOrderDetailRequest;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailResponse;
import com.sanisidro.restaurante.features.suppliers.service.PurchaseOrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-order-details")
@RequiredArgsConstructor
public class PurchaseOrderDetailController {

    private final PurchaseOrderDetailService detailService;

    @GetMapping
    public List<PurchaseOrderDetailResponse> getAll() {
        return detailService.getAll();
    }

    @GetMapping("/{id}")
    public PurchaseOrderDetailResponse getById(@PathVariable Long id) {
        return detailService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrderDetailResponse create(@Valid @RequestBody PurchaseOrderDetailRequest request) {
        return detailService.create(request);
    }

    @PutMapping("/{id}")
    public PurchaseOrderDetailResponse update(@PathVariable Long id, @Valid @RequestBody PurchaseOrderDetailRequest request) {
        return detailService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        detailService.delete(id);
    }

}
