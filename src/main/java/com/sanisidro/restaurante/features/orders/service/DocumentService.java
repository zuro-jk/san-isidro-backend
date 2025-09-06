package com.sanisidro.restaurante.features.orders.service;

import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentInOrderRequest;
import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentRequest;
import com.sanisidro.restaurante.features.orders.dto.document.response.DocumentResponse;
import com.sanisidro.restaurante.features.orders.model.Document;
import com.sanisidro.restaurante.features.orders.model.Order;
import com.sanisidro.restaurante.features.orders.repository.DocumentRepository;
import com.sanisidro.restaurante.features.orders.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final OrderRepository orderRepository;

    public List<DocumentResponse> getAll() {
        return documentRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public DocumentResponse getById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con id: " + id));
        return mapToResponse(document);
    }

    @Transactional
    public DocumentResponse create(DocumentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId()));

        Document document = Document.builder()
                .order(order)
                .type(request.getType())
                .number(request.getNumber())
                .amount(request.getAmount())
                .date(LocalDateTime.now())
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    @Transactional
    public Document createInOrder(Order order, DocumentInOrderRequest request) {
        Document document = Document.builder()
                .order(order)
                .type(request.getType())
                .number(request.getNumber())
                .date(request.getDate() != null ? request.getDate() : LocalDateTime.now())
                .amount(request.getAmount())
                .build();

        return documentRepository.save(document);
    }



    @Transactional
    public DocumentResponse update(Long id, DocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Documento no encontrado con id: " + id));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Orden no encontrada con id: " + request.getOrderId()));

        document.setOrder(order);
        document.setType(request.getType());
        document.setNumber(request.getNumber());
        document.setAmount(request.getAmount());

        return mapToResponse(documentRepository.save(document));
    }

    public void delete(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new EntityNotFoundException("Documento no encontrado con id: " + id);
        }
        documentRepository.deleteById(id);
    }



    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .orderId(document.getOrder().getId())
                .type(document.getType())
                .number(document.getNumber())
                .amount(document.getAmount())
                .date(document.getDate())
                .build();
    }

}
