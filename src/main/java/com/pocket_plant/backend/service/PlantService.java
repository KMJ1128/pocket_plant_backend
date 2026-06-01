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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantService {

    private final PlantRepository plantRepository;
    private final UserRepository userRepository;

    // 1. 유저별 식물 목록 조회
    @Transactional(readOnly = true)
    public List<PlantDTO> getMyPlants(Long userId) {
        return plantRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(PlantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 2. 식물 신규 등록
    public PlantDTO registerPlant(Long userId, PlantDTO requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다. ID: " + userId));

        Plant plant = requestDto.toEntity();
        plant.setUser(user); // 소유자 설정
        plant.setBookmarked(false); // 초기 등록 시 북마크 false

        return PlantDTO.fromEntity(plantRepository.save(plant));
    }

    // 3. 식물 정보 수정
    public PlantDTO updatePlant(Long userId, Long plantId, PlantDTO requestDto) {
        // 본인 소유의 식물인지 확인하며 조회
        Plant plant = plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new RuntimeException("식물을 찾을 수 없거나 수정 권한이 없습니다. Plant ID: " + plantId));

        // 데이터 업데이트
        plant.setName(requestDto.getName());
        plant.setSpecies(requestDto.getSpecies());
        // DTO 내부에서 문자열 -> LocalDate 변환 처리됨
        plant.setAdoptDate(requestDto.toEntity().getAdoptDate());
        plant.setAge(requestDto.getAge());
        plant.setPersonality(requestDto.getPersonality());

        // 영속성 컨텍스트에 의해 트랜잭션 종료 시 자동 update 쿼리 실행
        return PlantDTO.fromEntity(plant);
    }

    // 4. 식물 삭제
    public void deletePlant(Long userId, Long plantId) {
        // 본인 소유의 식물인지 확인하며 조회
        Plant plant = plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new RuntimeException("식물을 찾을 수 없거나 삭제 권한이 없습니다. Plant ID: " + plantId));

        plantRepository.delete(plant);
    }

    // 5. 북마크 토글
    public PlantDTO toggleBookmark(Long userId, Long plantId) {
        Plant plant = plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new RuntimeException("식물을 찾을 수 없거나 설정 권한이 없습니다. Plant ID: " + plantId));

        plant.setBookmarked(!plant.isBookmarked()); // 상태 반전

        return PlantDTO.fromEntity(plant);
    }
}