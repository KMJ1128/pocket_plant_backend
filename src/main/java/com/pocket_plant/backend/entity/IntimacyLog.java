package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "intimacy_log", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"plant_id", "action", "action_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntimacyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(nullable = false)
    private String action; // DAILY_LOGIN, CARE_ATTENTION, AI_CHAT, WATERING 등

    @Column(name = "action_date", nullable = false)
    private LocalDate actionDate;
}