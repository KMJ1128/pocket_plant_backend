package com.pocket_plant.backend.service;

import com.pocket_plant.backend.dto.PlantDTO;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.PlantRepository;
import com.pocket_plant.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PlantDTO> getMyPlants(Long userId) {
        return plantRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PlantDTO::fromEntity)
                .toList();
    }

    public PlantDTO registerPlant(
            Long userId,
            PlantDTO requestDto
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        Plant plant = requestDto.toEntity();
        plant.setUser(user);
        plant.setBookmarked(false);

        Plant savedPlant = plantRepository.save(plant);

        return PlantDTO.fromEntity(savedPlant);
    }

    public PlantDTO updatePlant(
            Long userId,
            Long plantId,
            PlantDTO requestDto
    ) {
        Plant plant = findOwnedPlant(userId, plantId);

        plant.setCharacter_id(requestDto.getCharacter_id());
        plant.setName(requestDto.getName());
        plant.setSpecies(requestDto.getSpecies());
        plant.setAdoptDate(requestDto.parseAdoptDate());
        plant.setAge(requestDto.getAge());
        plant.setPersonality(requestDto.getPersonality());
        plant.setImageUrl(emptyToNull(requestDto.getImageUri()));
        plant.setMacAddress(emptyToNull(requestDto.getMacAddress()));

        return PlantDTO.fromEntity(plant);
    }

    public void deletePlant(
            Long userId,
            Long plantId
    ) {
        Plant plant = findOwnedPlant(userId, plantId);
        plantRepository.delete(plant);
    }

    public PlantDTO toggleBookmark(
            Long userId,
            Long plantId
    ) {
        Plant plant = findOwnedPlant(userId, plantId);
        plant.setBookmarked(!plant.isBookmarked());

        return PlantDTO.fromEntity(plant);
    }

    private Plant findOwnedPlant(
            Long userId,
            Long plantId
    ) {
        return plantRepository
                .findByIdAndUserId(plantId, userId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "식물을 찾을 수 없거나 접근 권한이 없습니다."
                        )
                );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}