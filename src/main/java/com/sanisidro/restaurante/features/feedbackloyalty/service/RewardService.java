package com.sanisidro.restaurante.features.feedbackloyalty.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sanisidro.restaurante.features.feedbackloyalty.dto.reward.request.RewardRequest;
import com.sanisidro.restaurante.features.feedbackloyalty.dto.reward.response.RewardResponse;
import com.sanisidro.restaurante.features.feedbackloyalty.models.Reward;
import com.sanisidro.restaurante.features.feedbackloyalty.repository.RewardRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;

    // --- MAPPER ---
    // Un helper para convertir la Entidad a un DTO de Respuesta
    private RewardResponse mapToResponse(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .requiredPoints(reward.getRequiredPoints())
                .active(reward.getActive())
                .build();
    }

    // --- CRUD: READ (Todos) ---
    @Transactional(readOnly = true)
    public List<RewardResponse> getAllRewards() {
        return rewardRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- CRUD: READ (Solo Activos - para Clientes) ---
    @Transactional(readOnly = true)
    public List<RewardResponse> getActiveRewards() {
        return rewardRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // --- CRUD: READ (Uno solo) ---
    @Transactional(readOnly = true)
    public RewardResponse getRewardById(Long id) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada con id: " + id));
        return mapToResponse(reward);
    }

    // --- CRUD: CREATE ---
    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        Reward newReward = Reward.builder()
                .name(request.getName())
                .description(request.getDescription())
                .requiredPoints(request.getRequiredPoints())
                .active(request.getActive())
                .build();

        Reward savedReward = rewardRepository.save(newReward);
        return mapToResponse(savedReward);
    }

    // --- CRUD: UPDATE ---
    @Transactional
    public RewardResponse updateReward(Long id, RewardRequest request) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Recompensa no encontrada con id: " + id));

        reward.setName(request.getName());
        reward.setDescription(request.getDescription());
        reward.setRequiredPoints(request.getRequiredPoints());
        reward.setActive(request.getActive());

        Reward updatedReward = rewardRepository.save(reward);
        return mapToResponse(updatedReward);
    }

    // --- CRUD: DELETE ---
    @Transactional
    public void deleteReward(Long id) {
        if (!rewardRepository.existsById(id)) {
            throw new EntityNotFoundException("Recompensa no encontrada con id: " + id);
        }
        rewardRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<RewardResponse> getAvailableRewards() {
        return rewardRepository.findByActiveTrue()
                .stream()
                .map(r -> RewardResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .requiredPoints(r.getRequiredPoints())
                        .build())
                .toList();
    }
}
