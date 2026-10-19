package com.pocket_plant.backend.dto.watering;

import com.pocket_plant.backend.entity.WateringCommand;

import java.time.LocalDateTime;

public record WateringCommandResponse(
        Long commandId,
        Integer amountMl,
        Integer durationMs,
        String source,
        String status,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        String resultMessage
) {
    public static WateringCommandResponse from(WateringCommand command) {
        return new WateringCommandResponse(
                command.getId(),
                command.getAmountMl(),
                command.getDurationMs(),
                command.getSource().name(),
                command.getStatus().name(),
                command.getCreatedAt(),
                command.getCompletedAt(),
                command.getResultMessage()
        );
    }
}
