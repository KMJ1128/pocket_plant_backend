package com.pocket_plant.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PlantDiaryDto {

    @Getter
    @Setter
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
    public static class SensorSnapshotDto {
        private String temp;
        private String humidity;
        private String soil;
    }

    @Getter
    @Setter
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
    }
}