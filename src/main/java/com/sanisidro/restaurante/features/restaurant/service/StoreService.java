package com.sanisidro.restaurante.features.restaurant.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.core.exceptions.ResourceNotFoundException;
import com.sanisidro.restaurante.features.restaurant.dto.store.request.StoreRequest;
import com.sanisidro.restaurante.features.restaurant.dto.store.response.StoreResponse;
import com.sanisidro.restaurante.features.restaurant.model.Store;
import com.sanisidro.restaurante.features.restaurant.repository.StoreRepository;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<StoreResponse> getAllStores() {
        log.info("Buscando todas las sucursales");
        return storeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreResponse getStoreById(Long id) {
        log.info("Buscando sucursal con ID: {}", id);
        Store store = findStoreByIdOrElseThrow(id);
        return mapToResponse(store);
    }

    @Transactional
    public StoreResponse createStore(StoreRequest request) {
        log.info("Creando nueva sucursal con nombre: {}", request.getName());
        validateStoreRequest(request, null);

        storeRepository.findByNameIgnoreCase(request.getName()).ifPresent(existingStore -> {
            throw new EntityExistsException("Ya existe una sucursal con el nombre: " + request.getName());
        });

        Store store = Store.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .openTime(request.getOpenTime())
                .closeTime(request.getCloseTime())
                .build();

        Store savedStore = storeRepository.save(store);
        log.info("Sucursal creada con ID: {}", savedStore.getId());
        return mapToResponse(savedStore);
    }

    @Transactional
    public StoreResponse updateStore(Long id, StoreRequest request) {
        log.info("Actualizando sucursal con ID: {}", id);
        Store store = findStoreByIdOrElseThrow(id);
        validateStoreRequest(request, id);

        storeRepository.findByNameIgnoreCase(request.getName()).ifPresent(existingStore -> {
            if (!existingStore.getId().equals(id)) {
                throw new EntityExistsException("Ya existe otra sucursal con el nombre: " + request.getName());
            }
        });

        store.setName(request.getName());
        store.setAddress(request.getAddress());
        store.setPhone(request.getPhone());
        store.setOpenTime(request.getOpenTime());
        store.setCloseTime(request.getCloseTime());

        Store updatedStore = storeRepository.save(store);
        log.info("Sucursal actualizada con ID: {}", updatedStore.getId());
        return mapToResponse(updatedStore);
    }

    @Transactional
    public void deleteStore(Long id) {
        log.warn("Eliminando sucursal con ID: {}", id);
        Store store = findStoreByIdOrElseThrow(id);
        storeRepository.delete(store);
        log.info("Sucursal eliminada con ID: {}", id);
    }

    private Store findStoreByIdOrElseThrow(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con id: " + id));
    }

    private StoreResponse mapToResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .latitude(store.getLatitude())
                .longitude(store.getLongitude())
                .build();
    }

    private void validateStoreRequest(StoreRequest request, Long currentId) {
        if (request.getOpenTime() != null && request.getCloseTime() != null) {
            if (request.getCloseTime().isBefore(request.getOpenTime())
                    && !request.getCloseTime().equals(LocalTime.MIDNIGHT)) {
                if (request.getOpenTime().isBefore(LocalTime.of(6, 0))) {
                    throw new IllegalArgumentException(
                            "La hora de cierre no puede ser anterior a la hora de apertura.");
                }
            } else if (request.getOpenTime().equals(request.getCloseTime())) {
                throw new IllegalArgumentException("La hora de apertura y cierre no pueden ser iguales.");
            }
        }
    }

}
