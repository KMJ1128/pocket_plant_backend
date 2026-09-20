package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.WateringSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WateringSettingsRepository extends JpaRepository<WateringSettings, Long> {
    Optional<WateringSettings> findByPlantId(Long plantId);
}
