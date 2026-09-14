package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.DiseasePredictionResponse;
import com.pocket_plant.backend.service.PlantDiseaseService;
import com.pocket_plant.backend.service.PlantDiseaseService.DiseaseServerUnavailableException;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> predict(@RequestParam("image") MultipartFile image,
                                     @RequestParam(value = "species", defaultValue = "") String species) {
        try {
            DiseasePredictionResponse result = plantDiseaseService.predictDisease(image, species);
            return ResponseEntity.ok(result);
        } catch (DiseaseServerUnavailableException e) {
            DiseasePredictionResponse error = new DiseasePredictionResponse();
            error.setStatus("unavailable");
            error.setDiseaseSymptom(e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        } catch (Exception e) {
            DiseasePredictionResponse error = new DiseasePredictionResponse();
            error.setStatus("error");
            error.setDiseaseSymptom("진단 요청을 처리하지 못했습니다.");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
