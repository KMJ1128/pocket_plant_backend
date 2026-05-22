package com.pocket_plant.backend.dto.AI.Chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    private Long roomId;

    private String message;

}