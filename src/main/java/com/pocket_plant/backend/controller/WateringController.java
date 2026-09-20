package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.watering.*;
import com.pocket_plant.backend.service.WateringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WateringController {

    private final WateringService wateringService;

    @GetMapping("/api/plants/{plantId}/watering")
    public WateringSettingsResponse getSettings(
            Authentication authentication,
            @PathVariable Long plantId
    ) {
        return wateringService.getSettings(userId(authentication), plantId);
    }

    @PutMapping("/api/plants/{plantId}/watering")
    public WateringSettingsResponse updateSettings(
            Authentication authentication,
            @PathVariable Long plantId,
            @RequestBody WateringSettingsRequest request
    ) {
        return wateringService.updateSettings(userId(authentication), plantId, request);
    }

    @PostMapping("/api/plants/{plantId}/watering/manual")
    public WateringCommandResponse waterNow(
            Authentication authentication,
            @PathVariable Long plantId,
            @RequestBody ManualWateringRequest request
    ) {
        return wateringService.queueManual(userId(authentication), plantId, request);
    }

    @GetMapping("/api/plants/{plantId}/watering/history")
    public List<WateringCommandResponse> history(
            Authentication authentication,
            @PathVariable Long plantId
    ) {
        return wateringService.getHistory(userId(authentication), plantId);
    }

    @GetMapping("/api/device/{macAddress}/watering/next")
    public ResponseEntity<WateringCommandResponse> nextCommand(
            @PathVariable String macAddress
    ) {
        return wateringService.claimNext(macAddress)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/api/device/{macAddress}/watering/{commandId}/result")
    public WateringCommandResponse completeCommand(
            @PathVariable String macAddress,
            @PathVariable Long commandId,
            @RequestBody DeviceWateringResultRequest request
    ) {
        return wateringService.completeCommand(macAddress, commandId, request);
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long value)) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return value;
    }
}
