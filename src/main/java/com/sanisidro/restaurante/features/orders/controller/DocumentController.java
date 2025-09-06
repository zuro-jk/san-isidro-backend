package com.sanisidro.restaurante.features.orders.controller;

import com.sanisidro.restaurante.core.security.dto.ApiResponse;
import com.sanisidro.restaurante.features.orders.dto.document.request.DocumentRequest;
import com.sanisidro.restaurante.features.orders.dto.document.response.DocumentResponse;
import com.sanisidro.restaurante.features.orders.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAll() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Lista de documentos", documentService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Documento encontrado", documentService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> create(@Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Documento creado", documentService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> update(@PathVariable Long id, @Valid @RequestBody DocumentRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Documento actualizado", documentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        documentService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Documento eliminado", null));
    }

}
