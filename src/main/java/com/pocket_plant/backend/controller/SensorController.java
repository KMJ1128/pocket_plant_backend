package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.entity.SensorData;
import com.pocket_plant.backend.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final SensorDataRepository sensorDataRepository;

    @PostMapping
    public String receiveSensorData(@RequestBody Map<String, Float> data) {
        SensorData sensorData = SensorData.builder()
                .temperature(data.getOrDefault("temperature", 0f))
                .humidity(data.getOrDefault("humidity", 0f))
                .light(data.getOrDefault("light", 0f))
                .soil(data.getOrDefault("soil", 0f))
                .build();

        sensorDataRepository.save(sensorData);
        return "데이터 저장 성공";
    }
    // ㅎㅇ
}