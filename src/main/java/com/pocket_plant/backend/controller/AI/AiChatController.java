package com.pocket_plant.backend.controller.AI;

import com.pocket_plant.backend.dto.AI.Chat.SendMessageRequest;
import com.pocket_plant.backend.entity.AI.AiChatRoom;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.AI.AiChatRoomRepository;
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

    @PostMapping("/room")
    public ResponseEntity<?> createRoom(
            Authentication authentication
    ) {

        Long userId =
                (Long) authentication.getPrincipal();

        User user =
                userRepository.findById(userId)
                        .orElseThrow();

        AiChatRoom room =
                roomRepository.save(
                        AiChatRoom.builder()
                                .title("새 채팅")
                                .user(user)
                                .build()
                );

        return ResponseEntity.ok(room);
    }

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(
            @RequestBody SendMessageRequest request
    ) {

        AiChatRoom room =
                roomRepository.findById(
                                request.getRoomId()
                        )
                        .orElseThrow();

        String answer =
                aiChatService.sendMessage(
                        room,
                        request.getMessage()
                );

        return ResponseEntity.ok(answer);
    }


    //임시 테스트
    @GetMapping("/test")
    public ResponseEntity<?> test() {

        try {

            String answer =
                    aiChatService.testChat("너는 누구야?");

            return ResponseEntity.ok(answer);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }
}