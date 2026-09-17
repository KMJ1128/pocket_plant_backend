package com.pocket_plant.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SensorSnapshot {
    private String temp;
    private String humidity;
    private String soil;
}