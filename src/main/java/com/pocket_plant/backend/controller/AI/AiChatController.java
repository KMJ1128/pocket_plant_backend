package com.pocket_plant.backend.controller.AI;

import com.pocket_plant.backend.dto.AI.Chat.CreateRoomRequest;
import com.pocket_plant.backend.dto.AI.Chat.SendMessageRequest;
import com.pocket_plant.backend.entity.AI.AiChatRoom;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.AI.AiChatRoomRepository;
import com.pocket_plant.backend.repository.PlantRepository;
import com.pocket_plant.backend.repository.UserRepository;
import com.pocket_plant.backend.service.AI.AiChatService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-chat")
public class AiChatController {

    private final AiChatService aiChatService;

    private final AiChatRoomRepository roomRepository;

    private final UserRepository userRepository;

    private final PlantRepository plantRepository;

    @PostMapping("/room")
    public ResponseEntity<?> createRoom(
            Authentication authentication,
            @RequestBody CreateRoomRequest request
    ) {

        Long userId =
                (Long) authentication.getPrincipal();

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        Plant plant =
                plantRepository.findByIdAndUserId(
                                request.getPlantId(),
                                userId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "해당 식물을 찾을 수 없습니다."
                                )
                        );

        AiChatRoom room =
                roomRepository.save(
                        AiChatRoom.builder()
                                .title(
                                        plant.getName()
                                                + "와의 대화"
                                )
                                .user(user)
                                .plant(plant)
                                .build()
                );

        return ResponseEntity.ok(room);
    }

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(
            Authentication authentication,
            @RequestBody SendMessageRequest request
    ) {

        Long userId =
                (Long) authentication.getPrincipal();

        AiChatRoom room =
                roomRepository
                        .findByIdAndUserId(
                                request.getRoomId(),
                                userId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "채팅방을 찾을 수 없습니다."
                                )
                        );

        String answer =
                aiChatService.sendMessage(
                        room,
                        request.getMessage()
                );

        return ResponseEntity.ok(answer);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {

        try {

            String answer =
                    aiChatService.testChat(
                            "너는 누구야?"
                    );

            return ResponseEntity.ok(answer);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }
}