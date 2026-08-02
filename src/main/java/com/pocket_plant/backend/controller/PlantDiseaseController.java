package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.DiseasePredictionResponse;
import com.pocket_plant.backend.service.PlantDiseaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/disease")
public class PlantDiseaseController {

    private final PlantDiseaseService plantDiseaseService;

    public PlantDiseaseController(PlantDiseaseService plantDiseaseService) {
        this.plantDiseaseService = plantDiseaseService;
    }

    // 프론트엔드(React Native)에서 이 주소로 이미지를 보냄
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam("image") MultipartFile image) {
        try {
            DiseasePredictionResponse result = plantDiseaseService.predictDisease(image);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("진단 서버 연결 실패: " + e.getMessage());
        }
    }
}