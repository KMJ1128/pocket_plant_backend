package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Optional<SensorData> findTopByPlantIdOrderByRegDateDesc(Long plantId);

    List<SensorData> findByPlantIdOrderByRegDateAsc(Long plantId);
}