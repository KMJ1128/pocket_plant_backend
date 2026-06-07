package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SensorController {

    @PostMapping("/sensor")
    public String receiveSensorData(@RequestBody Map<String, Object> data) {
        // ESP32가 보낸 데이터를 스프링부트 콘솔창에 출력합니다.
        System.out.println("====== 센서 데이터 도착 ======");
        System.out.println("온도: " + data.get("temperature") + "°C");
        System.out.println("습도: " + data.get("humidity") + "%");
        System.out.println("밝기: " + data.get("light"));
        System.out.println("토양수분: " + data.get("soil"));
        
        return "데이터 잘 받았다!"; // ESP32에게 보낼 답장
    }
}