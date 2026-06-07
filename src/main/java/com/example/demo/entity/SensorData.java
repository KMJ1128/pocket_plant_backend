package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private float temperature; // 온도
    private float humidity;    // 습도
    private LocalDateTime regDate; // 받은 시간

    @PrePersist
    public void prePersist() {
        this.regDate = LocalDateTime.now();
    }
}