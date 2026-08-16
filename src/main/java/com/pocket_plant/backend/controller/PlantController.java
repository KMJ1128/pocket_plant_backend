package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.PlantDTO;
import com.pocket_plant.backend.service.PlantService;
import com.pocket_plant.backend.service.PlantSpeciesService;
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

    @GetMapping("/my")
    public ResponseEntity<List<PlantDTO>> getMyPlants(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                plantService.getMyPlants(
                        getUserId(authentication)
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<PlantDTO> registerPlant(
            Authentication authentication,
            @RequestBody PlantDTO requestDto
    ) {
        return ResponseEntity.ok(
                plantService.registerPlant(
                        getUserId(authentication),
                        requestDto
                )
        );
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<PlantDTO> updatePlant(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody PlantDTO requestDto
    ) {
        return ResponseEntity.ok(
                plantService.updatePlant(
                        getUserId(authentication),
                        id,
                        requestDto
                )
        );
    }

    @DeleteMapping("/edit/{id}")
    public ResponseEntity<Void> deletePlant(
            Authentication authentication,
            @PathVariable Long id
    ) {
        plantService.deletePlant(
                getUserId(authentication),
                id
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/bookmark")
    public ResponseEntity<PlantDTO> toggleBookmark(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                plantService.toggleBookmark(
                        getUserId(authentication),
                        id
                )
        );
    }

    @PostMapping(
            value = "/identify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> identifyPlantImage(
            @RequestParam("image") MultipartFile image
    ) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("이미지 파일이 없습니다.");
        }

        try {
            return ResponseEntity.ok(
                    plantSpeciesService.identifyPlantImage(image)
            );
        } catch (Exception exception) {
            return ResponseEntity.internalServerError()
                    .body(
                            "식물 인식 중 오류가 발생했습니다: "
                                    + exception.getMessage()
                    );
        }
    }

    private Long getUserId(Authentication authentication) {
        if (
                authentication == null
                        || !(authentication.getPrincipal() instanceof Long)
        ) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return (Long) authentication.getPrincipal();
    }
}