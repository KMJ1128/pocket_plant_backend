package com.pocket_plant.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PlantData {
    @Id
    private Long cntntNo; // 고유 컨텐츠 번호(기본키)
    private String plantName; // 식물명
    private String growhTp; // 생육 온도
    private String winterTemperature; // 겨울 최저 온도
    private String humidity; // 습도 정보
    private String waterCycleSpring; // 봄 물주기 원본 텍스트
    private String lightDemand; // 광도 요구도 원본 텍스트

    // === 센서 비교용 수치화 데이터 ===
    // 1. 토양 수분 데이터 (단위: %)
    private Integer waterThreshold;    // 관수 임계치 (이 값 이하로 떨어지면 물주기 알림)
    private Integer targetMoistureMin; // 적정 수분 최소값
    private Integer targetMoistureMax; // 적정 수분 최대값

    // 2. 광도 데이터 (단위: Lux)
    private Integer minLux; // 적정 최소 조도 (Lux)
    private Integer maxLux; // 적정 최대 조도 (Lux)
}