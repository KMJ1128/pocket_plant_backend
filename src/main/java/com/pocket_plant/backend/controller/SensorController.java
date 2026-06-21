package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.SensorDataDTO;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.SensorData;
import com.pocket_plant.backend.repository.PlantRepository;
import com.pocket_plant.backend.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SensorController {

    private final SensorDataRepository sensorDataRepository;
    private final PlantRepository plantRepository;

    // 1. ESP32 보드가 데이터를 보내는 곳
    @PostMapping("/sensor")
    public String receiveSensorData(
            @RequestBody Map<String, Object> data,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        System.out.println("\n====== [서버 로그] 센서 데이터 수신 이벤트 발생! ======");
        System.out.println("요청 보낸 기기 IP: " + request.getRemoteAddr());
        System.out.println("받은 원본 JSON 데이터: " + data);

        String macAddress = (String) data.get("macAddress");

        if (macAddress == null) {
            System.out.println("[서버 로그] ❌ 에러: MAC 주소가 누락되었습니다.");
            return "Fail: No MAC";
        }

        try {
            Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                    .orElseThrow(() -> new RuntimeException("해당 MAC 주소를 가진 식물이 없습니다."));

            Float temp = Float.valueOf(data.get("temperature").toString());
            Float hum = Float.valueOf(data.get("humidity").toString());
            Float light = Float.valueOf(data.get("light").toString());
            Float soil = Float.valueOf(data.get("moisture").toString());

            SensorData sensorData = SensorData.builder()
                    .plant(plant)
                    .temperature(temp)
                    .humidity(hum)
                    .light(light)
                    .soil(soil)
                    .build();

            sensorDataRepository.save(sensorData);

            System.out.println("🌱 센서 데이터 DB 저장 성공! 식물명: " + plant.getName());

            return "DB 저장 완료!";
        } catch (Exception e) {
            System.out.println("❌ 데이터 저장 실패: " + e.getMessage());
            return "저장 실패: " + e.getMessage();
        }
    }

    // 2. 최신 센서 데이터 1개 조회
    @GetMapping("/sensor/latest/{macAddress}")
    public ResponseEntity<?> getLatestData(
            @PathVariable String macAddress
    ) {
        System.out.println("\n====== [서버 로그] 최신 센서 데이터 조회 요청 ======");
        System.out.println("조회 MAC 주소: " + macAddress);

        try {
            Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                    .orElseThrow(() -> new RuntimeException("해당 기기가 등록된 식물이 없습니다."));

            SensorData latestData =
                    sensorDataRepository.findTopByPlantIdOrderByRegDateDesc(plant.getId())
                            .orElseThrow(() -> new RuntimeException("아직 수신된 센서 데이터가 없습니다."));

            System.out.println("✅ 최신 센서 데이터 조회 성공");
            System.out.println("plantId: " + plant.getId());
            System.out.println("sensorId: " + latestData.getId());

            return ResponseEntity.ok(
                    SensorDataDTO.fromEntity(latestData)
            );

        } catch (Exception e) {
            System.out.println("❌ 최신 센서 데이터 조회 실패: " + e.getMessage());

            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    // 3. 처음 센싱한 데이터부터 현재까지 전체 조회
    @GetMapping("/sensor/history/{macAddress}")
    public ResponseEntity<?> getSensorHistory(
            @PathVariable String macAddress
    ) {
        System.out.println("\n====== [서버 로그] 전체 센서 히스토리 조회 요청 ======");
        System.out.println("조회 MAC 주소: " + macAddress);

        try {
            Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                    .orElseThrow(() -> new RuntimeException("해당 기기가 등록된 식물이 없습니다."));

            List<SensorData> history =
                    sensorDataRepository.findByPlantIdOrderByRegDateAsc(plant.getId());

            List<SensorDataDTO> result =
                    history.stream()
                            .map(SensorDataDTO::fromEntity)
                            .collect(Collectors.toList());

            System.out.println("✅ 전체 센서 히스토리 조회 성공");
            System.out.println("plantId: " + plant.getId());
            System.out.println("조회된 데이터 개수: " + result.size());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.out.println("❌ 센서 히스토리 조회 실패: " + e.getMessage());

            return ResponseEntity.badRequest().body(
                    Map.of("message", e.getMessage())
            );
        }
    }
}