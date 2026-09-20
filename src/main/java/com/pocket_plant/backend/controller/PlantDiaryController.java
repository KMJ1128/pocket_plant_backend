package com.pocket_plant.backend.controller; // 프로젝트 패키지명에 맞춤

import com.pocket_plant.backend.dto.PlantDiaryDto;
import com.pocket_plant.backend.service.PlantDiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
@RequiredArgsConstructor
public class PlantDiaryController {

    private final PlantDiaryService diaryService;

    // 목록 조회 (GET /api/plants/{plantId}/diaries)
    @GetMapping("/{plantId}/diaries")
    public ResponseEntity<List<PlantDiaryDto.Response>> getDiaries(@PathVariable Long plantId) {
        return ResponseEntity.ok(diaryService.getEntriesByPlant(plantId));
    }

    // 단건 상세 조회 (GET /api/plants/diaries/{entryId})
    @GetMapping("/diaries/{entryId}")
    public ResponseEntity<PlantDiaryDto.Response> getDiary(@PathVariable Long entryId) {
        return ResponseEntity.ok(diaryService.getEntry(entryId));
    }

    // 작성 (POST /api/plants/diaries)
    @PostMapping("/diaries")
    public ResponseEntity<Long> createDiary(@Valid @RequestBody PlantDiaryDto.CreateRequest req) {
        Long id = diaryService.createEntry(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    // 수정 (PUT /api/plants/diaries/{entryId})
    @PutMapping("/diaries/{entryId}")
    public ResponseEntity<Void> updateDiary(
            @PathVariable Long entryId,
            @Valid @RequestBody PlantDiaryDto.UpdateRequest req) {
        diaryService.updateEntry(entryId, req);
        return ResponseEntity.noContent().build();
    }

    // 삭제 (DELETE /api/plants/diaries/{entryId})
    @DeleteMapping("/diaries/{entryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable Long entryId) {
        diaryService.deleteEntry(entryId);
        return ResponseEntity.noContent().build();
    }
}