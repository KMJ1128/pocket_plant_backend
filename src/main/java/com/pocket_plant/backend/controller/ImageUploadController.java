package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
public class ImageUploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadImage(
            Authentication authentication,
            @RequestParam("image") MultipartFile image
    ) throws Exception {

        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }

        String filename =
                imageStorageService.save(image);

        String imageUri =
                ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path("/uploads/")
                        .path(filename)
                        .toUriString();

        return ResponseEntity.ok(
                Map.of("imageUri", imageUri)
        );
    }
}