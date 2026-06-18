package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.entity.PlantData;
import com.pocket_plant.backend.service.PlantDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlantDataController {

    private final PlantDataService plantDataService;

    @GetMapping("/api/plant/fetch")
    public String fetchPlants() {
        try {
            String apiKey = "20260526NSCGTAZMTKBFS3FZUTRQ";
            plantDataService.fetchAndSavePlants(apiKey);
            return "식물 데이터 수집 및 DB 저장 완료!";
        } catch (Exception e) {
            e.printStackTrace();
            return "수집 실패: " + e.getMessage();
        }
    }

    @org.springframework.web.bind.annotation.CrossOrigin // 리액트 네이티브의 접근을 허용하기 위해 추가합니다.
    @GetMapping("/api/plant/env")
    public org.springframework.http.ResponseEntity<?> getPlantEnvironment(@RequestParam("name") String name) {

        System.out.println("식물 데이터 요청");
        java.util.List<PlantData> plants = plantDataService.searchPlants(name);

        // 검색된 식물이 없을 경우 404 에러 반환
        if (plants.isEmpty()) {
            return org.springframework.http.ResponseEntity
                    .status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body("해당 식물을 찾을 수 없습니다.");
        }

        // 가장 정확한 첫 번째 식물의 환경 데이터(Plant 객체)를 그대로 리턴 (스프링이 자동으로 JSON 변환)
        return org.springframework.http.ResponseEntity.ok(plants.get(0));
    }

}
