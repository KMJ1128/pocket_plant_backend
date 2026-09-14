package com.pocket_plant.backend.service.AI;

import com.pocket_plant.backend.dto.AI.Chat.ChatResponse;
import com.pocket_plant.backend.entity.SensorData;
import com.pocket_plant.backend.repository.SensorStatisticsProjection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class SensorContextFactory {

    static final int PERIOD_DAYS = 7;
    private static final Duration STALE_AFTER = Duration.ofHours(6);

    private SensorContextFactory() {
    }

    static ChatResponse.SensorContext create(
            SensorData latest,
            SensorStatisticsProjection statistics,
            LocalDateTime now
    ) {
        long sampleCount = statistics == null || statistics.getSampleCount() == null
                ? 0L
                : statistics.getSampleCount();

        if (latest == null || sampleCount == 0L) {
            return new ChatResponse.SensorContext(
                    false, true, null, PERIOD_DAYS, 0, null, null
            );
        }

        boolean stale = latest.getRegDate() == null
                || latest.getRegDate().isBefore(now.minus(STALE_AFTER));
        int safeSampleCount = (int) Math.min(sampleCount, Integer.MAX_VALUE);

        return new ChatResponse.SensorContext(
                true,
                stale,
                latest.getRegDate(),
                PERIOD_DAYS,
                safeSampleCount,
                new ChatResponse.SensorReading(
                        latest.getTemperature(), latest.getHumidity(), latest.getLight(), latest.getSoil()
                ),
                new ChatResponse.SensorStatistics(
                        metric(statistics.getTemperatureAverage(), statistics.getTemperatureMinimum(), statistics.getTemperatureMaximum(), safeSampleCount),
                        metric(statistics.getHumidityAverage(), statistics.getHumidityMinimum(), statistics.getHumidityMaximum(), safeSampleCount),
                        metric(statistics.getLightAverage(), statistics.getLightMinimum(), statistics.getLightMaximum(), safeSampleCount),
                        metric(statistics.getSoilAverage(), statistics.getSoilMinimum(), statistics.getSoilMaximum(), safeSampleCount)
                )
        );
    }

    static ChatResponse.SensorContext create(
            List<SensorData> readings,
            LocalDateTime now
    ) {
        List<SensorData> safeReadings = readings == null
                ? List.of()
                : readings.stream()
                .filter(Objects::nonNull)
                .filter(value -> value.getRegDate() != null)
                .toList();

        if (safeReadings.isEmpty()) {
            return new ChatResponse.SensorContext(
                    false,
                    true,
                    null,
                    PERIOD_DAYS,
                    0,
                    null,
                    null
            );
        }

        SensorData latest = safeReadings.get(safeReadings.size() - 1);
        boolean stale = latest.getRegDate().isBefore(now.minus(STALE_AFTER));

        return new ChatResponse.SensorContext(
                true,
                stale,
                latest.getRegDate(),
                PERIOD_DAYS,
                safeReadings.size(),
                new ChatResponse.SensorReading(
                        latest.getTemperature(),
                        latest.getHumidity(),
                        latest.getLight(),
                        latest.getSoil()
                ),
                new ChatResponse.SensorStatistics(
                        summarize(safeReadings, SensorData::getTemperature),
                        summarize(safeReadings, SensorData::getHumidity),
                        summarize(safeReadings, SensorData::getLight),
                        summarize(safeReadings, SensorData::getSoil)
                )
        );
    }

    private static ChatResponse.MetricStatistics summarize(
            List<SensorData> readings,
            Function<SensorData, Float> valueExtractor
    ) {
        List<Double> values = readings.stream()
                .map(valueExtractor)
                .filter(Objects::nonNull)
                .map(Float::doubleValue)
                .filter(Double::isFinite)
                .toList();

        if (values.isEmpty()) {
            return new ChatResponse.MetricStatistics(null, null, null, 0);
        }

        double average = values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        double minimum = values.stream().mapToDouble(Double::doubleValue).min().orElseThrow();
        double maximum = values.stream().mapToDouble(Double::doubleValue).max().orElseThrow();

        return new ChatResponse.MetricStatistics(
                round(average),
                round(minimum),
                round(maximum),
                values.size()
        );
    }

    private static ChatResponse.MetricStatistics metric(
            Double average,
            Float minimum,
            Float maximum,
            int samples
    ) {
        return new ChatResponse.MetricStatistics(
                average == null ? null : round(average),
                minimum == null ? null : round(minimum.doubleValue()),
                maximum == null ? null : round(maximum.doubleValue()),
                average == null ? 0 : samples
        );
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
