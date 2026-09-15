package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.SensorDataDTO;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.SensorData;
import com.pocket_plant.backend.repository.PlantRepository;
import com.pocket_plant.backend.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SensorController {

    private final SensorDataRepository sensorDataRepository;
    private final PlantRepository plantRepository;

    // 1. ESP32 보드가 데이터를 보내는 곳
    @PostMapping("/sensor")
    public ResponseEntity<?> receiveSensorData(
            @RequestBody Map<String, Object> data
    ) {
        String macAddress = String.valueOf(data.getOrDefault("macAddress", ""))
                .trim()
                .toUpperCase(Locale.ROOT);

        if (macAddress.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "MAC 주소가 누락되었습니다.")
            );
        }

        try {
            Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                    .orElse(null);

            if (plant == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("message", "해당 MAC 주소를 가진 식물이 없습니다.")
                );
            }

            Float temp = requiredFiniteFloat(data, "temperature");
            Float hum = requiredFiniteFloat(data, "humidity");
            Float light = requiredFiniteFloat(data, "light");
            Float soil = requiredFiniteFloat(data, "moisture");

            SensorData sensorData = SensorData.builder()
                    .plant(plant)
                    .temperature(temp)
                    .humidity(hum)
                    .light(light)
                    .soil(soil)
                    .build();

            sensorDataRepository.save(sensorData);

            log.debug("센서 데이터 저장 완료: plantId={}", plant.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of("status", "saved", "plantId", plant.getId())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("센서 데이터 저장 실패: macAddress={}", macAddress, e);
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "센서 데이터를 저장하지 못했습니다.")
            );
        }
    }

    // 2. 최신 센서 데이터 1개 조회
    @GetMapping("/sensor/latest/{macAddress}")
    public ResponseEntity<?> getLatestData(
            @PathVariable String macAddress
    ) {
        Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                .orElse(null);

        if (plant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "해당 기기가 등록된 식물이 없습니다.")
            );
        }

        SensorData latestData = sensorDataRepository
                .findTopByPlantIdOrderByRegDateDesc(plant.getId())
                .orElse(null);

        if (latestData == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(SensorDataDTO.fromEntity(latestData));
    }

    // 3. 그래프용 최근 센서 데이터 조회 (무제한 전체 조회 방지)
    @GetMapping("/sensor/history/{macAddress}")
    public ResponseEntity<?> getSensorHistory(
            @PathVariable String macAddress
    ) {
        Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                .orElse(null);

        if (plant == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("message", "해당 기기가 등록된 식물이 없습니다.")
            );
        }

        List<SensorData> history =
                sensorDataRepository.findTop500ByPlantIdOrderByRegDateDesc(plant.getId());

        // DB에서는 최신 500개만 가져오고 앱에는 시간순으로 전달한다.
        Collections.reverse(history);

        List<SensorDataDTO> result =
                history.stream()
                        .map(SensorDataDTO::fromEntity)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private Float requiredFiniteFloat(Map<String, Object> data, String field) {
        Object rawValue = data.get(field);
        if (rawValue == null) {
            throw new IllegalArgumentException(field + " 값이 누락되었습니다.");
        }

        final float value;
        try {
            value = Float.parseFloat(rawValue.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " 값이 숫자가 아닙니다.");
        }

        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException(field + " 값이 유효하지 않습니다.");
        }

        return value;
    }
}
