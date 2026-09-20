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

        return diaryRepository.save(diary).getId();
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
        res.setDate(diary.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        res.setDay(diary.getDay());
        // 트랜잭션이 끝난 뒤 JSON 직렬화가 실행되므로 Hibernate의 지연 로딩
        // 컬렉션을 DTO에 그대로 넘기지 않고 세션 안에서 일반 List로 복사한다.
        List<String> moods = new ArrayList<>(diary.getMoods());
        List<String> photoUrls = new ArrayList<>(diary.getPhotoUrls());
        res.setMoods(moods);
        res.setMood(!moods.isEmpty() ? moods.get(0) : "sprout");
        res.setPhotoUris(photoUrls);
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
