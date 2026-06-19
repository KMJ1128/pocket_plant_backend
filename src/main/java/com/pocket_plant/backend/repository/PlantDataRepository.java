package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.PlantData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantDataRepository extends JpaRepository<PlantData, Long> {
    List<PlantData> findByPlantNameContaining(String keyword);
}
