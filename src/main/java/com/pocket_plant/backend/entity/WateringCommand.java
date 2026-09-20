package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "watering_commands",
        indexes = {
                @Index(name = "idx_watering_command_plant_status", columnList = "plant_id,status"),
                @Index(name = "idx_watering_command_created", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WateringCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "amount_ml", nullable = false)
    private Integer amountMl;

    @Column(name = "duration_ms", nullable = false)
    private Integer durationMs;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WateringSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WateringCommandStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "result_message", length = 500)
    private String resultMessage;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
