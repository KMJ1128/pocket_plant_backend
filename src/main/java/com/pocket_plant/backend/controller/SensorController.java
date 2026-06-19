package com.pocket_plant.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 💡 중요: 리액트 네이티브 앱이 서버에 접속할 수 있도록 허용해 줍니다.
public class SensorController {

    // 최신 데이터를 메모리에 임시로 담아둘 보관함(우체통)입니다.
    private final Map<String, Object> latestData = new ConcurrentHashMap<>();

    // 1. ESP32 보드가 데이터를 보내는 곳 (POST)
    @PostMapping("/sensor")
    public String receiveSensorData(@RequestBody Map<String, Object> data) {
        System.out.println("====== ESP32로부터 데이터 도착 ======");
        System.out.println(data);
        // 보관함에 최신 데이터로 업데이트합니다.
        latestData.putAll(data);

        System.out.println(latestData.get("light"));

        return "데이터 저장 완료!";
    }

    // 2. ⭐️ 리액트 네이티브 앱이 최신 데이터를 가져가는 곳 (GET)
    @GetMapping("/sensor/latest")
    public Map<String, Object> getLatestData() {
        // 앱이 요청하면 보관하고 있던 최신 데이터를 JSON 형태로 돌려줍니다.
//        double temperature = (double) latestData.get("temperature"); // 온도
//        double humidity = (double) latestData.get("humidity"); //습도
//        int light = (int) latestData.get("light"); // 조도
//        int moisture = (int) latestData.get("moisture"); // 토양수분
        System.out.println("센서데이터 요청");
        return latestData;
    }
}
// dd