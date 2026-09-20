package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "watering_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WateringSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false, unique = true)
    private Plant plant;

    @Builder.Default
    @Column(name = "automatic_enabled", nullable = false)
    private boolean automaticEnabled = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WateringMode mode = WateringMode.MANUAL;

    @Builder.Default
    @Column(name = "amount_ml", nullable = false)
    private Integer amountMl = 100;

    @Builder.Default
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays = 7;

    @Builder.Default
    @Column(name = "soil_moisture_threshold", nullable = false)
    private Float soilMoistureThreshold = 30F;

    @Builder.Default
    @Column(name = "minimum_interval_hours", nullable = false)
    private Integer minimumIntervalHours = 24;

    @Builder.Default
    @Column(name = "pump_ml_per_second", nullable = false)
    private Float pumpMlPerSecond = 30F;

    @Column(name = "last_watered_at")
    private LocalDateTime lastWateredAt;

    @Column(name = "next_watering_at")
    private LocalDateTime nextWateringAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
