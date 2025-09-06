package com.sanisidro.restaurante.features.suppliers.service;

import com.sanisidro.restaurante.core.exceptions.BadRequestException;
import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.request.PurchaseOrderRequest;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorder.response.PurchaseOrderResponse;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailInOrderResponse;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailResponse;
import com.sanisidro.restaurante.features.suppliers.enums.PurchaseOrderStatus;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import com.sanisidro.restaurante.features.suppliers.model.Supplier;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderRepository;
import com.sanisidro.restaurante.features.suppliers.repository.SupplierRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
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
    private final ProductRepository productRepository;

    public List<PurchaseOrderResponse> getAll() {
        return purchaseOrderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PurchaseOrderResponse getById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Orden de compra no encontrada con id: " + id));
        return mapToResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado con id: " + request.getSupplierId()));

        PurchaseOrderStatus status = parseStatus(request.getStatus());

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .date(LocalDateTime.now())
                .status(status)
                .build();

        Set<PurchaseOrderDetail> details = new LinkedHashSet<>();
        request.getDetails().forEach(d -> {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id: " + d.getProductId()));

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .build();

            details.add(detail);
        });

        order.replaceDetails(details);

        BigDecimal total = details.stream()
                .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(total);

        return mapToResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de compra no encontrada con id: " + id));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Proveedor no encontrado con id: " + request.getSupplierId()));

        PurchaseOrderStatus status = parseStatus(request.getStatus());

        order.setSupplier(supplier);
        order.setDate(LocalDateTime.now());
        order.setStatus(status);

        Set<PurchaseOrderDetail> details = new LinkedHashSet<>();
        request.getDetails().forEach(d -> {
            Product product = productRepository.findById(d.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Producto no encontrado con id: " + d.getProductId()));

            PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                    .order(order)
                    .product(product)
                    .quantity(d.getQuantity())
                    .unitPrice(d.getUnitPrice())
                    .build();

            details.add(detail);
        });

        order.replaceDetails(details);

        BigDecimal total = order.getDetails().stream()
                .map(det -> det.getUnitPrice().multiply(BigDecimal.valueOf(det.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotal(total);

        return mapToResponse(purchaseOrderRepository.save(order));
    }

    public void delete(Long id) {
        if (!purchaseOrderRepository.existsById(id)) {
            throw new EntityNotFoundException("Orden de compra no encontrada con id: " + id);
        }
        purchaseOrderRepository.deleteById(id);
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .supplierId(order.getSupplier().getId())
                .supplierName(order.getSupplier().getName())
                .date(order.getDate())
                .status(order.getStatus())
                .total(order.getTotal())
                .details(
                        order.getDetails().stream()
                                .map(d -> PurchaseOrderDetailInOrderResponse.builder()
                                        .id(d.getId())
                                        .productId(d.getProduct().getId())
                                        .productName(d.getProduct().getName())
                                        .quantity(d.getQuantity())
                                        .unitPrice(d.getUnitPrice())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }

    /**
     * Convierte un String en PurchaseOrderStatus.
     * Si es null o vacío, devuelve PENDING como valor por defecto.
     */
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
