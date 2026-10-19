package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.IntimacyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface IntimacyLogRepository extends JpaRepository<IntimacyLog, Long> {
    // 오늘 해당 식물이 이 행동을 이미 했는지 여부 확인
    boolean existsByPlantIdAndActionAndActionDate(Long plantId, String action, LocalDate actionDate);
}