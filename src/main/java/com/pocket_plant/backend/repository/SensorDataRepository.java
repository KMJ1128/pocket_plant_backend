package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Optional<SensorData> findTopByPlantIdOrderByRegDateDesc(Long plantId);

    List<SensorData> findTop500ByPlantIdOrderByRegDateDesc(Long plantId);

    List<SensorData> findByPlantIdAndRegDateGreaterThanEqualOrderByRegDateAsc(
            Long plantId,
            LocalDateTime from
    );

    @Query("""
            select count(s.id) as sampleCount,
                   avg(s.temperature) as temperatureAverage,
                   min(s.temperature) as temperatureMinimum,
                   max(s.temperature) as temperatureMaximum,
                   avg(s.humidity) as humidityAverage,
                   min(s.humidity) as humidityMinimum,
                   max(s.humidity) as humidityMaximum,
                   avg(s.light) as lightAverage,
                   min(s.light) as lightMinimum,
                   max(s.light) as lightMaximum,
                   avg(s.soil) as soilAverage,
                   min(s.soil) as soilMinimum,
                   max(s.soil) as soilMaximum
              from SensorData s
             where s.plant.id = :plantId
               and s.regDate >= :from
            """)
    SensorStatisticsProjection summarizeSince(
            @Param("plantId") Long plantId,
            @Param("from") LocalDateTime from
    );
}
