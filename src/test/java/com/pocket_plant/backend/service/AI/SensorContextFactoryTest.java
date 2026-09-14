package com.pocket_plant.backend.service.AI;

import com.pocket_plant.backend.dto.AI.Chat.ChatResponse;
import com.pocket_plant.backend.entity.SensorData;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensorContextFactoryTest {

    @Test
    void summarizesLatestAndSevenDayReadings() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 15, 1, 0);
        SensorData first = reading(now.minusDays(6), 20f, 40f, 100f, 25f);
        SensorData latest = reading(now.minusMinutes(10), 30f, 60f, 300f, 45f);

        ChatResponse.SensorContext context =
                SensorContextFactory.create(List.of(first, latest), now);

        assertThat(context.available()).isTrue();
        assertThat(context.stale()).isFalse();
        assertThat(context.sampleCount()).isEqualTo(2);
        assertThat(context.latest().temperatureCelsius()).isEqualTo(30f);
        assertThat(context.recent().temperatureCelsius().average()).isEqualTo(25.0);
        assertThat(context.recent().lightRaw().maximum()).isEqualTo(300.0);
    }

    @Test
    void marksMissingAndOldDataAsUnavailableOrStale() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 15, 1, 0);

        assertThat(SensorContextFactory.create(List.of(), now).available()).isFalse();
        assertThat(SensorContextFactory.create(
                List.of(reading(now.minusHours(7), 20f, null, null, null)), now
        ).stale()).isTrue();
    }

    private SensorData reading(
            LocalDateTime observedAt,
            Float temperature,
            Float humidity,
            Float light,
            Float soil
    ) {
        SensorData data = new SensorData();
        data.setRegDate(observedAt);
        data.setTemperature(temperature);
        data.setHumidity(humidity);
        data.setLight(light);
        data.setSoil(soil);
        return data;
    }
}
