package com.pocket_plant.backend.dto;

import com.pocket_plant.backend.entity.PlantDiary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

public class PlantDiaryDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotNull
        private Long plantId;
        private Integer day;
        private List<String> moods;
        @Size(max = 3, message = "사진은 최대 3장까지 등록 가능합니다.")
        private List<String> photoUrls;
        @NotBlank(message = "메모는 필수 입력 항목입니다.")
        @Size(max = 300, message = "메모는 300자 이하로 입력해주세요.")
        private String note;
        private SensorSnapshotDto sensorSnapshot;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private List<String> moods;
        @Size(max = 3, message = "사진은 최대 3장까지 등록 가능합니다.")
        private List<String> photoUrls;
        @NotBlank(message = "메모는 필수 입력 항목입니다.")
        @Size(max = 300, message = "메모는 300자 이하로 입력해주세요.")
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SensorSnapshotDto {
        private String temp;
        private String humidity;
        private String soil;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long plantId;
        private String date;
        private Integer day;
        private List<String> moods;
        private String mood; // 프론트 단일 mood 하위 호환
        private List<String> photoUris;
        private String note;
        private SensorSnapshotDto sensorSnapshot;

        public static Response fromEntity(PlantDiary diary) {
            if (diary == null) {
                return null;
            }

            // 1. photoUrls 지연 로딩 컬렉션 복사
            List<String> photos = diary.getPhotoUrls() != null
                    ? new ArrayList<>(diary.getPhotoUrls())
                    : new ArrayList<>();

            // 2. moods 지연 로딩 컬렉션 복사
            List<String> moodList = diary.getMoods() != null
                    ? new ArrayList<>(diary.getMoods())
                    : new ArrayList<>();

            // 3. 센서 스냅샷 매핑
            SensorSnapshotDto snapshotDto = null;
            if (diary.getSensorSnapshot() != null) {
                snapshotDto = SensorSnapshotDto.builder()
                        .temp(diary.getSensorSnapshot().getTemp())
                        .humidity(diary.getSensorSnapshot().getHumidity())
                        .soil(diary.getSensorSnapshot().getSoil())
                        .build();
            }

            return Response.builder()
                    .id(diary.getId())
                    .plantId(diary.getPlantId()) // <- 이 부분으로 수정
                    .date(diary.getCreatedAt() != null ? diary.getCreatedAt().toString() : null)
                    .day(diary.getDay())
                    .moods(moodList)
                    .mood(moodList.isEmpty() ? null : moodList.get(0))
                    .photoUris(photos)
                    .note(diary.getNote())
                    .sensorSnapshot(snapshotDto)
                    .build();
        }
    }
}