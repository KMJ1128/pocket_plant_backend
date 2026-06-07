package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.service.PlantSpeciesService;
import com.pocket_plant.backend.dto.PlantDTO;
import com.pocket_plant.backend.entity.MsgEntity;
import com.pocket_plant.backend.service.PlantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;
    private final PlantSpeciesService plantSpeciesService;
    // [GET] /api/plants/my - 내 식물 목록 조회 (Main 화면)
    @GetMapping("/my") // "/my" 엔드포인트로 변경하여 내 식물 목록 조회를 명확히 표현)
    public ResponseEntity<List<PlantDTO>> getAllPlants(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<PlantDTO> plants = plantService.getMyPlants(userId);
        return ResponseEntity.ok(plants);
    }

    // [POST] /api/plants - 식물 등록 (PlantRegister 화면 - 등록 모드)
    @PostMapping("/register") // "/register" 엔드포인트로 변경하여 식물 등록을 명확히 표현)
    public ResponseEntity<MsgEntity> registerPlant(Authentication authentication, @RequestBody PlantDTO plantDto) {
        Long userId = (Long) authentication.getPrincipal();
        PlantDTO createdPlant = plantService.registerPlant(userId, plantDto);
        return ResponseEntity.ok(new MsgEntity("식물 등록 성공", createdPlant));
    }

    // [PUT] /api/plants/edit/{id} - 식물 수정 (PlantRegister 화면 - 수정 모드)
    @PutMapping("/edit/{id}")
    public ResponseEntity<MsgEntity> updatePlant(
            Authentication authentication,
            @PathVariable("id") Long id,
            @RequestBody PlantDTO plantDto) {
        Long userId = (Long) authentication.getPrincipal();
        PlantDTO updatedPlant = plantService.updatePlant(userId, id, plantDto);
        return ResponseEntity.ok(new MsgEntity("식물 수정 성공", updatedPlant));
    }

    // [DELETE] /api/plants/edit/{id} - 식물 삭제 (Main 화면 ActionSheet)
    @DeleteMapping("/edit/{id}")
    public ResponseEntity<MsgEntity> deletePlant(Authentication authentication, @PathVariable("id") Long id) {
        Long userId = (Long) authentication.getPrincipal();
        plantService.deletePlant(userId, id);
        return ResponseEntity.ok(new MsgEntity("식물 삭제 성공", id));
    }

    // [PATCH] /api/plants/{id}/bookmark - 북마크 토글 (Main 화면 카드 별 아이콘)
    // Partial Update이므로 PUT 대신 PATCH가 적합합니다.
    @PatchMapping("/{id}/bookmark")
    public ResponseEntity<PlantDTO> toggleBookmark(Authentication authentication, @PathVariable("id") Long id) {
        Long userId = (Long) authentication.getPrincipal();
        PlantDTO updatedPlant = plantService.toggleBookmark(userId, id);
        return ResponseEntity.ok(updatedPlant);
    }

    @PostMapping(value = "/identify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> identifyPlantImage(@RequestParam("image") MultipartFile image) {
        try {
            // 빈 파일이 들어왔는지 체크
            if (image.isEmpty()) {
                return ResponseEntity.badRequest().body("이미지 파일이 없습니다.");
            }

            // 사진 분석 서비스 호출
            Object result = plantSpeciesService.identifyPlantImage(image);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("식물 인식 중 오류 발생: " + e.getMessage());
        }
    }



}