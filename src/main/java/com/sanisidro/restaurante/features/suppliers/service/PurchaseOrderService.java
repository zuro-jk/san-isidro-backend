package com.sanisidro.restaurante.features.suppliers.service;

import com.sanisidro.restaurante.core.exceptions.BadRequestException;
import com.sanisidro.restaurante.features.products.exceptions.IngredientNotFoundException;
import com.sanisidro.restaurante.features.products.exceptions.InventoryNotFoundException;
import com.sanisidro.restaurante.features.products.model.Ingredient;
import com.sanisidro.restaurante.features.products.model.Inventory;
import com.sanisidro.restaurante.features.products.repository.IngredientRepository;
import com.sanisidro.restaurante.features.products.repository.InventoryRepository;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.request.PurchaseOrderRequest;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.response.PurchaseOrderResponse;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import com.sanisidro.restaurante.features.suppliers.exceptions.PurchaseOrderNotFoundException;
import com.sanisidro.restaurante.features.suppliers.exceptions.SupplierNotFoundException;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderRepository;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;
    private final InventoryRepository inventoryRepository;

    public List<PurchaseOrderResponse> getAll() {
        return purchaseOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Orden de compra no encontrada con id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + request.getSupplierId()));

        PurchaseOrderStatus status = parseStatus(request.getStatus());

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .date(LocalDateTime.now())
                .status(status)
                .build();

        Set<PurchaseOrderDetail> details = buildDetails(request, order);
        order.replaceDetails(details);
        order.setTotal(calculateTotal(details));

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        if (status == PurchaseOrderStatus.RECEIVED) {
            details.forEach(d -> updateInventoryStock(d.getIngredient().getId(), BigDecimal.valueOf(d.getQuantity()), true));
        }

        return mapToResponse(savedOrder);
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Orden de compra no encontrada con id: " + id));

        PurchaseOrderStatus oldStatus = order.getStatus();
        PurchaseOrderStatus newStatus = parseStatus(request.getStatus());

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new SupplierNotFoundException("Proveedor no encontrado con id: " + request.getSupplierId()));

        order.setSupplier(supplier);
        order.setDate(LocalDateTime.now());
        order.setStatus(newStatus);

        Set<PurchaseOrderDetail> newDetails = buildDetails(request, order);

        if (oldStatus == PurchaseOrderStatus.RECEIVED && newStatus != PurchaseOrderStatus.RECEIVED) {
            order.getDetails().forEach(d -> updateInventoryStock(d.getIngredient().getId(), BigDecimal.valueOf(d.getQuantity()), false));
        }

        order.replaceDetails(newDetails);
        order.setTotal(calculateTotal(newDetails));

        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);

        if (oldStatus != PurchaseOrderStatus.RECEIVED && newStatus == PurchaseOrderStatus.RECEIVED) {
            newDetails.forEach(d -> updateInventoryStock(d.getIngredient().getId(), BigDecimal.valueOf(d.getQuantity()), true));
        }

        return mapToResponse(updatedOrder);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new PurchaseOrderNotFoundException("Orden de compra no encontrada con id: " + id));

        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            order.getDetails().forEach(d -> updateInventoryStock(d.getIngredient().getId(), BigDecimal.valueOf(d.getQuantity()), false));
        }

        purchaseOrderRepository.delete(order);
    }

    private void updateInventoryStock(Long ingredientId, BigDecimal qty, boolean increase) {
        Inventory inventory = inventoryRepository.findByIngredientId(ingredientId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventario no encontrado para ingrediente: " + ingredientId));

        if (increase) {
            inventory.increaseStock(qty);
        } else {
            inventory.decreaseStock(qty);
        }

        inventoryRepository.saveAndFlush(inventory);
    }

    private Set<PurchaseOrderDetail> buildDetails(PurchaseOrderRequest request, PurchaseOrder order) {
        Set<PurchaseOrderDetail> details = new LinkedHashSet<>();
        request.getDetails().forEach(d -> {
            Ingredient ingredient = ingredientRepository.findById(d.getIngredientId())
                    .orElseThrow(() -> new IngredientNotFoundException("Ingrediente no encontrado con id: " + d.getIngredientId()));

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .order(order)
                    .ingredient(ingredient)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .build();

            details.add(detail);
        });
        return details;
    }

    private BigDecimal calculateTotal(Set<PurchaseOrderDetail> details) {
        return details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .supplierId(order.getSupplier().getId())
                .supplierName(order.getSupplier().getCompanyName())
                .date(order.getDate())
                .status(order.getStatus())
                .total(order.getTotal())
                .details(
                        order.getDetails().stream()
                                .map(d -> PurchaseOrderDetailInOrderResponse.builder()
                                        .id(d.getId())
                                        .ingredientId(d.getIngredient().getId())
                                        .ingredientName(d.getIngredient().getName())
                                        .quantity(d.getQuantity())
                                        .unitPrice(d.getUnitPrice())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }

    private PurchaseOrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return PurchaseOrderStatus.PENDING;
        }
        try {
            return PurchaseOrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Estado inválido: " + status + ". Valores permitidos: " +
                            Arrays.toString(PurchaseOrderStatus.values())
            );
        }
    }
}
