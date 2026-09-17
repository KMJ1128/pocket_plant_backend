package com.pocket_plant.backend.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plant_diary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PlantDiary {

    // 테이블 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 식물 아이디
    @Column(nullable = false)
    private Long plantId;

    @CreatedDate
    // updatable = false : 업데이트 되더라도 날짜는 그대로 유지
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Integer day;

    // 기분 모드 구분
    // 모드 구분을 위해 별도 테이블 생성
    @ElementCollection
    @CollectionTable(name="plant_diary_moods", joinColumns = @JoinColumn(name="diary_id"))
    @Column(name = "mood")
    private List<String> moods = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "plant_diary_photos", joinColumns = @JoinColumn(name = "diary_id"))
    @Column(name = "photo_url")
    private List<String> photoUrls = new ArrayList<>();

    // 일지 내용
    @Column(nullable = false, length = 300)
    private String note;

    // 센서 데이터
    @Embedded
    private SensorSnapshot sensorSnapshot;

    @Builder
    public PlantDiary(Long plantId, Integer day, List<String> moods, List<String> photoUrls, String note, SensorSnapshot sensorSnapshot) {
        this.plantId = plantId;
        this.day = day;
        this.moods = moods != null ? moods : new ArrayList<>();
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
        this.note = note;
        this.sensorSnapshot = sensorSnapshot;
    }

    public void update(List<String> moods, List<String> photoUrls, String note) {
        this.moods = moods != null ? moods : new ArrayList<>();
        this.photoUrls = photoUrls != null ? photoUrls : new ArrayList<>();
        this.note = note;
    }
}
