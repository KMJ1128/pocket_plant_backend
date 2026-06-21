package com.pocket_plant.backend.dto;

import com.pocket_plant.backend.entity.SensorData;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SensorDataDTO {

    private Long id;
    private Float temperature;
    private Float humidity;
    private Float light;
    private Float soil;
    private LocalDateTime regDate;

    public static SensorDataDTO fromEntity(SensorData sensorData) {
        return SensorDataDTO.builder()
                .id(sensorData.getId())
                .temperature(sensorData.getTemperature())
                .humidity(sensorData.getHumidity())
                .light(sensorData.getLight())
                .soil(sensorData.getSoil())
                .regDate(sensorData.getRegDate())
                .build();
    }
}