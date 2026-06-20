package com.pocket_plant.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SensorController {

    // 💡 [변경] 식별자가 Long에서 String(MAC 주소)으로 바뀜
    // 사물함 번호표(MAC 주소) -> 사물함 안의 내용물(온습도 데이터)
    private final Map<String, Map<String, Object>> latestDataMap = new ConcurrentHashMap<>();

    // 1. ESP32 보드가 데이터를 보내는 곳
    @PostMapping("/sensor")
    public String receiveSensorData(@RequestBody Map<String, Object> data) {
        // ESP32가 보낸 JSON에서 MAC 주소를 추출
        String macAddress = (String) data.get("macAddress");

        System.out.println("====== MAC [" + macAddress + "] 기기로부터 데이터 도착 ======");

        // 해당 MAC 주소 사물함 칸에 데이터를 덮어씀
        latestDataMap.put(macAddress, data);

        return "데이터 저장 완료!";
    }

    // 2. 리액트 네이티브 앱이 "특정 기기"의 데이터를 가져가는 곳
    // 주소 예시: /api/sensor/latest/10:00:3B:D1:3F:C8 (이런 식으로 앱에서 요청)
    @GetMapping("/sensor/latest/{macAddress}")
    public Map<String, Object> getLatestData(@PathVariable String macAddress) {
        System.out.println("MAC [" + macAddress + "] 센서데이터 요청");

        return latestDataMap.getOrDefault(macAddress, Map.of("message", "아직 수신된 데이터가 없습니다."));
    }
}