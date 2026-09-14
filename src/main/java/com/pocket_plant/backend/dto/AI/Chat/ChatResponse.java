package com.pocket_plant.backend.dto.AI.Chat;

import java.time.LocalDateTime;
public record ChatResponse(
        String answer,
        Long plantId,
        LocalDateTime generatedAt,
        SensorContext sensorContext
) {
    public record SensorContext(
            boolean available,
            boolean stale,
            LocalDateTime latestObservedAt,
            int periodDays,
            int sampleCount,
            SensorReading latest,
            SensorStatistics recent
    ) {
    }

    public record SensorReading(
            Float temperatureCelsius,
            Float airHumidityPercent,
            Float lightRaw,
            Float soilMoistureRaw
    ) {
    }

    public record SensorStatistics(
            MetricStatistics temperatureCelsius,
            MetricStatistics airHumidityPercent,
            MetricStatistics lightRaw,
            MetricStatistics soilMoistureRaw
    ) {
    }

    public record MetricStatistics(
            Double average,
            Double minimum,
            Double maximum,
            int samples
    ) {
    }
}
