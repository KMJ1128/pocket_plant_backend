package com.pocket_plant.backend.service;

import com.pocket_plant.backend.dto.PlantDTO;
import com.pocket_plant.backend.entity.IntimacyLog;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.IntimacyLogRepository;
import com.pocket_plant.backend.repository.PlantRepository;
import com.pocket_plant.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final IntimacyLogRepository intimacyLogRepository;

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

    // 컨트롤러(유저 요청)용 친밀도 증가
    public PlantDTO increaseIntimacy(Long userId, Long plantId, String action) {
        Plant plant = findOwnedPlant(userId, plantId);
        applyIntimacy(plant, action);
        return PlantDTO.fromEntity(plant);
    }

    // 내부 서비스(일지 작성 등)용 친밀도 증가
    public void increaseIntimacyByPlantId(Long plantId, String action) {
        Plant plant = plantRepository.findById(plantId)
                .orElse(null);
        if (plant != null) {
            applyIntimacy(plant, action);
        }
    }

    // 공통 친밀도 계산 및 제한 처리 로직
    private void applyIntimacy(Plant plant, String action) {
        LocalDate today = LocalDate.now();
        String upperAction = (action != null) ? action.toUpperCase() : "CARE_ATTENTION";

        boolean isOncePerDay = switch (upperAction) {
            case "DAILY_LOGIN", "CARE_ATTENTION", "AI_CHAT" -> true;
            case "WATERING", "DIARY_WRITE", "DISEASE_PREDICT" -> false;
            default -> false;
        };

        if (isOncePerDay) {
            boolean alreadyEarned = intimacyLogRepository
                    .existsByPlantIdAndActionAndActionDate(plant.getId(), upperAction, today);
            if (alreadyEarned) {
                return;
            }
        }

        Long currentIntimacy = plant.getIntimacy() != null ? plant.getIntimacy() : 0L;
        long scoreToAdd = getScoreByAction(upperAction);
        plant.setIntimacy(Math.min(100L, currentIntimacy + scoreToAdd));

        if (isOncePerDay) {
            intimacyLogRepository.save(IntimacyLog.builder()
                    .plant(plant)
                    .action(upperAction)
                    .actionDate(today)
                    .build());
        }
    }

    private long getScoreByAction(String action) {
        return switch (action) {
            case "DAILY_LOGIN" -> 1L;     // 매일 접속: +1점
            case "WATERING" -> 3L;        // 물 주기: +3점
            case "AI_CHAT" -> 2L;         // AI 대화: +2점
            case "DIARY_WRITE" -> 3L;     // 일지 작성: +3점
            case "CARE_ATTENTION" -> 1L;  // 식물 관심 주기: +1점
            case "DISEASE_PREDICT" -> 4L; // 질병 진단하기: +4점
            default -> 1L;
        };
    }
}