package com.pocket_plant.backend.dto.watering;

import com.pocket_plant.backend.entity.WateringMode;

public record WateringSettingsRequest(
        Boolean automaticEnabled,
        WateringMode mode,
        Integer amountMl,
        Integer intervalDays,
        Float soilMoistureThreshold,
        Integer minimumIntervalHours,
        Float pumpMlPerSecond
) {
}
