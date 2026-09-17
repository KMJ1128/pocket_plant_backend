package com.pocket_plant.backend.repository;


import com.pocket_plant.backend.entity.PlantDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantDiaryRepository extends JpaRepository<PlantDiary, Long> {
    List<PlantDiary> findByPlantIdOrderByCreatedAtDesc(Long plantId);
}