package com.pocket_plant.backend.dto.watering;

import com.pocket_plant.backend.entity.WateringSettings;

import java.time.LocalDateTime;

public record WateringSettingsResponse(
        Long plantId,
        boolean automaticEnabled,
        String mode,
        Integer amountMl,
        Integer intervalDays,
        Float soilMoistureThreshold,
        Integer minimumIntervalHours,
        Float pumpMlPerSecond,
        LocalDateTime lastWateredAt,
        LocalDateTime nextWateringAt,
        Float latestSoilMoisture,
        LocalDateTime latestSensorAt
) {
    public static WateringSettingsResponse from(
            WateringSettings settings,
            Float latestSoilMoisture,
            LocalDateTime latestSensorAt
    ) {
        return new WateringSettingsResponse(
                settings.getPlant().getId(),
                settings.isAutomaticEnabled(),
                settings.getMode().name(),
                settings.getAmountMl(),
                settings.getIntervalDays(),
                settings.getSoilMoistureThreshold(),
                settings.getMinimumIntervalHours(),
                settings.getPumpMlPerSecond(),
                settings.getLastWateredAt(),
                settings.getNextWateringAt(),
                latestSoilMoisture,
                latestSensorAt
        );
    }
}
