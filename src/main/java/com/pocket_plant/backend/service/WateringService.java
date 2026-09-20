package com.pocket_plant.backend.service;

import com.pocket_plant.backend.dto.watering.*;
import com.pocket_plant.backend.entity.*;
import com.pocket_plant.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class WateringService {

    private static final List<WateringCommandStatus> ACTIVE_STATUSES = List.of(
            WateringCommandStatus.PENDING,
            WateringCommandStatus.DISPATCHED
    );
    private static final int MIN_AMOUNT_ML = 10;
    private static final int MAX_AMOUNT_ML = 500;
    private static final int MAX_PUMP_DURATION_MS = 30_000;

    private final PlantRepository plantRepository;
    private final SensorDataRepository sensorDataRepository;
    private final WateringSettingsRepository settingsRepository;
    private final WateringCommandRepository commandRepository;

    @Transactional(readOnly = true)
    public WateringSettingsResponse getSettings(Long userId, Long plantId) {
        Plant plant = findOwnedPlant(userId, plantId);
        WateringSettings settings = settingsRepository.findByPlantId(plantId)
                .orElseGet(() -> defaultSettings(plant));
        return response(settings);
    }

    public WateringSettingsResponse updateSettings(
            Long userId,
            Long plantId,
            WateringSettingsRequest request
    ) {
        Plant plant = findOwnedPlant(userId, plantId);
        validateSettings(request);

        WateringSettings settings = settingsRepository.findByPlantId(plantId)
                .orElseGet(() -> defaultSettings(plant));

        settings.setAutomaticEnabled(Boolean.TRUE.equals(request.automaticEnabled()));
        settings.setMode(request.mode());
        settings.setAmountMl(request.amountMl());
        settings.setIntervalDays(request.intervalDays());
        settings.setSoilMoistureThreshold(request.soilMoistureThreshold());
        settings.setMinimumIntervalHours(request.minimumIntervalHours());
        settings.setPumpMlPerSecond(request.pumpMlPerSecond());

        if (settings.isAutomaticEnabled() && settings.getMode() == WateringMode.MANUAL) {
            throw new IllegalArgumentException("수동 모드에서는 자동 급수를 켤 수 없습니다.");
        }

        if (settings.isAutomaticEnabled() && settings.getMode() == WateringMode.INTERVAL) {
            LocalDateTime base = Optional.ofNullable(settings.getLastWateredAt())
                    .orElse(LocalDateTime.now());
            settings.setNextWateringAt(base.plusDays(settings.getIntervalDays()));
        } else {
            settings.setNextWateringAt(null);
        }

        settingsRepository.save(settings);
        return response(settings);
    }

    public WateringCommandResponse queueManual(
            Long userId,
            Long plantId,
            ManualWateringRequest request
    ) {
        Plant plant = findOwnedPlant(userId, plantId);
        if (plant.getMacAddress() == null || plant.getMacAddress().isBlank()) {
            throw new IllegalStateException("먼저 식물에 ESP32를 연결해주세요.");
        }

        int amountMl = requireAmount(request.amountMl());
        WateringSettings settings = settingsRepository.findByPlantId(plantId)
                .orElseGet(() -> settingsRepository.save(defaultSettings(plant)));

        cleanupExpired(plantId, LocalDateTime.now());
        if (commandRepository.existsByPlantIdAndStatusIn(plantId, ACTIVE_STATUSES)) {
            throw new IllegalStateException("이미 실행 대기 중인 급수 명령이 있습니다.");
        }

        return WateringCommandResponse.from(
                createCommand(plant, settings, amountMl, WateringSource.MANUAL)
        );
    }

    @Transactional(readOnly = true)
    public List<WateringCommandResponse> getHistory(Long userId, Long plantId) {
        findOwnedPlant(userId, plantId);
        return commandRepository.findTop50ByPlantIdOrderByCreatedAtDesc(plantId)
                .stream()
                .map(WateringCommandResponse::from)
                .toList();
    }

    public Optional<WateringCommandResponse> claimNext(String rawMacAddress) {
        String macAddress = normalizeMac(rawMacAddress);
        Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다."));
        WateringSettings settings = settingsRepository.findByPlantId(plant.getId())
                .orElseGet(() -> settingsRepository.save(defaultSettings(plant)));

        LocalDateTime now = LocalDateTime.now();
        cleanupExpired(plant.getId(), now);
        createAutomaticCommandIfDue(plant, settings, now);

        Optional<WateringCommand> command = commandRepository
                .findFirstByPlantIdAndStatusAndExpiresAtAfterOrderByCreatedAtAsc(
                        plant.getId(), WateringCommandStatus.PENDING, now
                );

        if (command.isEmpty()) {
            command = commandRepository
                    .findFirstByPlantIdAndStatusAndDispatchedAtBeforeAndExpiresAtAfterOrderByCreatedAtAsc(
                            plant.getId(),
                            WateringCommandStatus.DISPATCHED,
                            now.minusMinutes(2),
                            now
                    );
        }

        command.ifPresent(value -> {
            value.setStatus(WateringCommandStatus.DISPATCHED);
            value.setDispatchedAt(now);
        });

        return command.map(WateringCommandResponse::from);
    }

    public WateringCommandResponse completeCommand(
            String rawMacAddress,
            Long commandId,
            DeviceWateringResultRequest request
    ) {
        String macAddress = normalizeMac(rawMacAddress);
        Plant plant = plantRepository.findFirstByMacAddressOrderByIdDesc(macAddress)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 기기입니다."));
        WateringCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("급수 명령을 찾을 수 없습니다."));

        if (!command.getPlant().getId().equals(plant.getId())) {
            throw new IllegalArgumentException("기기와 급수 명령이 일치하지 않습니다.");
        }
        if (command.getStatus() == WateringCommandStatus.COMPLETED) {
            return WateringCommandResponse.from(command);
        }

        boolean success = Boolean.TRUE.equals(request.success());
        LocalDateTime now = LocalDateTime.now();
        command.setCompletedAt(now);
        command.setResultMessage(trimMessage(request.message()));
        command.setStatus(success
                ? WateringCommandStatus.COMPLETED
                : WateringCommandStatus.FAILED);

        if (success) {
            WateringSettings settings = settingsRepository.findByPlantId(plant.getId())
                    .orElseGet(() -> settingsRepository.save(defaultSettings(plant)));
            settings.setLastWateredAt(now);
            if (settings.isAutomaticEnabled() && settings.getMode() == WateringMode.INTERVAL) {
                settings.setNextWateringAt(now.plusDays(settings.getIntervalDays()));
            }
        }

        return WateringCommandResponse.from(command);
    }

    private void createAutomaticCommandIfDue(
            Plant plant,
            WateringSettings settings,
            LocalDateTime now
    ) {
        if (!settings.isAutomaticEnabled() || settings.getMode() == WateringMode.MANUAL) {
            return;
        }
        if (commandRepository.existsByPlantIdAndStatusIn(plant.getId(), ACTIVE_STATUSES)) {
            return;
        }

        boolean due = false;
        WateringSource source = WateringSource.INTERVAL;

        if (settings.getMode() == WateringMode.INTERVAL) {
            due = settings.getNextWateringAt() != null
                    && !settings.getNextWateringAt().isAfter(now);
        } else if (settings.getMode() == WateringMode.SOIL) {
            SensorData latest = sensorDataRepository
                    .findTopByPlantIdOrderByRegDateDesc(plant.getId())
                    .orElse(null);
            boolean freshSensor = latest != null
                    && latest.getRegDate() != null
                    && latest.getRegDate().isAfter(now.minusMinutes(15));
            boolean cooldownPassed = settings.getLastWateredAt() == null
                    || settings.getLastWateredAt()
                    .plusHours(settings.getMinimumIntervalHours())
                    .isBefore(now);
            due = freshSensor
                    && latest.getSoil() != null
                    && latest.getSoil() <= settings.getSoilMoistureThreshold()
                    && cooldownPassed;
            source = WateringSource.SOIL;
        }

        if (due) {
            createCommand(plant, settings, settings.getAmountMl(), source);
        }
    }

    private WateringCommand createCommand(
            Plant plant,
            WateringSettings settings,
            int amountMl,
            WateringSource source
    ) {
        int durationMs = calculateDurationMs(amountMl, settings.getPumpMlPerSecond());
        LocalDateTime now = LocalDateTime.now();
        return commandRepository.save(WateringCommand.builder()
                .plant(plant)
                .amountMl(amountMl)
                .durationMs(durationMs)
                .source(source)
                .status(WateringCommandStatus.PENDING)
                .createdAt(now)
                .expiresAt(now.plusMinutes(10))
                .build());
    }

    private int calculateDurationMs(int amountMl, Float pumpMlPerSecond) {
        if (pumpMlPerSecond == null || !Float.isFinite(pumpMlPerSecond)
                || pumpMlPerSecond < 5F || pumpMlPerSecond > 100F) {
            throw new IllegalArgumentException("펌프 보정값은 초당 5~100mL여야 합니다.");
        }
        long durationMs = Math.round(amountMl / pumpMlPerSecond * 1000D);
        if (durationMs > MAX_PUMP_DURATION_MS) {
            throw new IllegalArgumentException("한 번의 펌프 작동 시간은 30초를 넘을 수 없습니다.");
        }
        return (int) Math.max(durationMs, 300L);
    }

    private void cleanupExpired(Long plantId, LocalDateTime now) {
        commandRepository.findByPlantIdAndStatusIn(plantId, ACTIVE_STATUSES)
                .stream()
                .filter(command -> command.getExpiresAt() != null
                        && !command.getExpiresAt().isAfter(now))
                .forEach(command -> command.setStatus(WateringCommandStatus.EXPIRED));
    }

    private WateringSettingsResponse response(WateringSettings settings) {
        SensorData latest = sensorDataRepository
                .findTopByPlantIdOrderByRegDateDesc(settings.getPlant().getId())
                .orElse(null);
        return WateringSettingsResponse.from(
                settings,
                latest == null ? null : latest.getSoil(),
                latest == null ? null : latest.getRegDate()
        );
    }

    private WateringSettings defaultSettings(Plant plant) {
        return WateringSettings.builder().plant(plant).build();
    }

    private Plant findOwnedPlant(Long userId, Long plantId) {
        return plantRepository.findByIdAndUserId(plantId, userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "식물을 찾을 수 없거나 접근 권한이 없습니다."
                ));
    }

    private void validateSettings(WateringSettingsRequest request) {
        if (request == null || request.mode() == null) {
            throw new IllegalArgumentException("급수 모드를 선택해주세요.");
        }
        requireAmount(request.amountMl());
        requireRange(request.intervalDays(), 1, 90, "급수 간격은 1~90일이어야 합니다.");
        requireRange(request.minimumIntervalHours(), 6, 720, "최소 재급수 간격은 6~720시간이어야 합니다.");
        if (request.soilMoistureThreshold() == null
                || !Float.isFinite(request.soilMoistureThreshold())
                || request.soilMoistureThreshold() < 5F
                || request.soilMoistureThreshold() > 80F) {
            throw new IllegalArgumentException("토양 수분 기준은 5~80%여야 합니다.");
        }
        calculateDurationMs(request.amountMl(), request.pumpMlPerSecond());
    }

    private int requireAmount(Integer amountMl) {
        requireRange(amountMl, MIN_AMOUNT_ML, MAX_AMOUNT_ML,
                "급수량은 10~500mL여야 합니다.");
        return amountMl;
    }

    private void requireRange(Integer value, int min, int max, String message) {
        if (value == null || value < min || value > max) {
            throw new IllegalArgumentException(message);
        }
    }

    private String normalizeMac(String macAddress) {
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("MAC 주소가 누락되었습니다.");
        }
        return macAddress.trim().toUpperCase(Locale.ROOT);
    }

    private String trimMessage(String message) {
        if (message == null) return null;
        String trimmed = message.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
