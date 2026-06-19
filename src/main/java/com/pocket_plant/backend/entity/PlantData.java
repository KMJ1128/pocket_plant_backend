package com.pocket_plant.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
public class PlantData {
    @Id
    private Long cntntNo; // 고유 컨텐츠 번호(기본키)
    private String plantName; // 식물명
    private String growhTp; // 생육 온도
    private String winterTemperature; // 겨울 최저 온도
    private String humidity; // 습도 정보
    private String waterCycleSpring; // 봄 물주기

    public PlantData() {}

    public Long getCntntNo() {
        return cntntNo;
    }

    public void setCntntNo(Long cntntNo) {
        this.cntntNo = cntntNo;
    }

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getGrowhTp() {
        return growhTp;
    }

    public void setGrowhTp(String growhTp) {
        this.growhTp = growhTp;
    }

    public String getWinterTemperature() {
        return winterTemperature;
    }

    public void setWinterTemperature(String winterTemperature) {
        this.winterTemperature = winterTemperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getWaterCycleSpring() {
        return waterCycleSpring;
    }

    public void setWaterCycleSpring(String waterCycleSpring) {
        this.waterCycleSpring = waterCycleSpring;
    }


}
