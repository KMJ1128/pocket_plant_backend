package com.pocket_plant.backend.service;

import com.pocket_plant.backend.entity.PlantDiary;
import com.pocket_plant.backend.dto.PlantDiaryDto;
import com.pocket_plant.backend.repository.PlantDiaryRepository;
import com.pocket_plant.backend.entity.SensorSnapshot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantDiaryService {

    private final PlantDiaryRepository diaryRepository;
    private final PlantService plantService; // PlantService 주입 추가

    public List<PlantDiaryDto.Response> getEntriesByPlant(Long plantId) {
        return diaryRepository.findByPlantIdOrderByCreatedAtDesc(plantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public PlantDiaryDto.Response getEntry(Long entryId) {
        PlantDiary diary = diaryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다. ID: " + entryId));
        return toResponse(diary);
    }

    @Transactional
    public Long createEntry(PlantDiaryDto.CreateRequest req) {
        SensorSnapshot snapshot = null;
        if (req.getSensorSnapshot() != null) {
            snapshot = SensorSnapshot.builder()
                    .temp(req.getSensorSnapshot().getTemp())
                    .humidity(req.getSensorSnapshot().getHumidity())
                    .soil(req.getSensorSnapshot().getSoil())
                    .build();
        }

        PlantDiary diary = PlantDiary.builder()
                .plantId(req.getPlantId())
                .day(req.getDay())
                .moods(req.getMoods())
                .photoUrls(req.getPhotoUrls())
                .note(req.getNote())
                .sensorSnapshot(snapshot)
                .build();

        Long savedId = diaryRepository.save(diary).getId();

        // 일지 작성 성공 시 친밀도 +3점 가산 연동
        try {
            plantService.increaseIntimacyByPlantId(req.getPlantId(), "DIARY_WRITE");
        } catch (Exception e) {
            System.err.println("일지 작성 친밀도 반영 실패: " + e.getMessage());
        }

        return savedId;
    }

    @Transactional
    public void updateEntry(Long entryId, PlantDiaryDto.UpdateRequest req) {
        PlantDiary diary = diaryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("일지를 찾을 수 없습니다. ID: " + entryId));
        diary.update(req.getMoods(), req.getPhotoUrls(), req.getNote());
    }

    @Transactional
    public void deleteEntry(Long entryId) {
        diaryRepository.deleteById(entryId);
    }

    private PlantDiaryDto.Response toResponse(PlantDiary diary) {
        PlantDiaryDto.Response res = new PlantDiaryDto.Response();
        res.setId(diary.getId());
        res.setPlantId(diary.getPlantId());
        res.setDate(diary.getCreatedAt() != null
                ? diary.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null);
        res.setDay(diary.getDay());

        // moods 프록시 컬렉션을 새 ArrayList로 복사하여 세션 분리 에러 방지
        List<String> moodList = diary.getMoods() != null
                ? new ArrayList<>(diary.getMoods())
                : new ArrayList<>();
        res.setMoods(moodList);
        res.setMood(!moodList.isEmpty() ? moodList.get(0) : "sprout");

        // photoUrls 프록시 컬렉션을 새 ArrayList로 복사하여 세션 분리 에러 방지
        List<String> photoList = diary.getPhotoUrls() != null
                ? new ArrayList<>(diary.getPhotoUrls())
                : new ArrayList<>();
        res.setPhotoUris(photoList);

        res.setNote(diary.getNote());

        if (diary.getSensorSnapshot() != null) {
            PlantDiaryDto.SensorSnapshotDto sDto = new PlantDiaryDto.SensorSnapshotDto();
            sDto.setTemp(diary.getSensorSnapshot().getTemp());
            sDto.setHumidity(diary.getSensorSnapshot().getHumidity());
            sDto.setSoil(diary.getSensorSnapshot().getSoil());
            res.setSensorSnapshot(sDto);
        }
        return res;
    }
}