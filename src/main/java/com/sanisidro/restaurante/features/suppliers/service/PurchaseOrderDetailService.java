package com.sanisidro.restaurante.features.suppliers.service;

import com.sanisidro.restaurante.features.products.model.Product;
import com.sanisidro.restaurante.features.products.repository.ProductRepository;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.request.PurchaseOrderDetailRequest;
import com.sanisidro.restaurante.features.suppliers.dto.purchaseorderdetail.response.PurchaseOrderDetailResponse;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrder;
import com.sanisidro.restaurante.features.suppliers.model.PurchaseOrderDetail;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderDetailRepository;
import com.sanisidro.restaurante.features.suppliers.repository.PurchaseOrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderDetailService {

    private final PurchaseOrderDetailRepository detailRepository;
    private final PurchaseOrderRepository orderRepository;
    private final ProductRepository productRepository;

    public List<PurchaseOrderDetailResponse> getAll() {
        return detailRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PurchaseOrderDetailResponse getById(Long id) {
        PurchaseOrderDetail detail = detailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado con id: " + id));
        return mapToResponse(detail);
    }

    public PurchaseOrderDetailResponse create(PurchaseOrderDetailRequest request) {
        PurchaseOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden de compra no encontrada con id: " + request.getOrderId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        PurchaseOrderDetail detail = PurchaseOrderDetail.builder()
                .order(order)
                .product(product)
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();

        return mapToResponse(detailRepository.save(detail));
    }

    public PurchaseOrderDetailResponse update(Long id, PurchaseOrderDetailRequest request) {
        PurchaseOrderDetail detail = detailRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Detalle no encontrado con id: " + id));

        PurchaseOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden de compra no encontrada con id: " + request.getOrderId()));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + request.getProductId()));

        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(request.getQuantity());
        detail.setUnitPrice(request.getUnitPrice());

        return mapToResponse(detailRepository.save(detail));
    }

    public void delete(Long id) {
        if (!detailRepository.existsById(id)) {
            throw new EntityNotFoundException("Detalle no encontrado con id: " + id);
        }
        detailRepository.deleteById(id);
    }

    private PurchaseOrderDetailResponse mapToResponse(PurchaseOrderDetail detail) {
        return PurchaseOrderDetailResponse.builder()
                .id(detail.getId())
                .orderId(detail.getOrder().getId())
                .productId(detail.getProduct().getId())
                .productName(detail.getProduct().getName())
                .quantity(detail.getQuantity())
                .unitPrice(detail.getUnitPrice())
                .build();
    }
}
