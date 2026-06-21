package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.Plant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant, Long> {

    // 특정 유저의 식물 목록 조회 (최신순 정렬)
    List<Plant> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 식물 ID와 유저 ID로 특정 식물 조회 (보안 및 권한 확인용)
    Optional<Plant> findByIdAndUserId(Long plantId, Long userId);

    Optional<Plant> findFirstByMacAddressOrderByIdDesc(String macAddress);
}