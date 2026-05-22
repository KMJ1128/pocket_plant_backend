package com.pocket_plant.backend.service.AI;

import com.pocket_plant.backend.entity.AI.AiChatMessage;
import com.pocket_plant.backend.entity.AI.AiChatRoom;
import com.pocket_plant.backend.repository.AI.AiChatMessageRepository;
import com.pocket_plant.backend.repository.AI.AiChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final WebClient.Builder webClientBuilder;

    private final AiChatRoomRepository roomRepository;

    private final AiChatMessageRepository messageRepository;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.model}")
    private String modelName;

    public String sendMessage(
            AiChatRoom room,
            String userMessage
    ) {

        // 사용자 메시지 저장
        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.USER)
                        .content(userMessage)
                        .build()
        );

        String body = """
        {
          "model":"%s",
          "messages":[
            {
              "role":"user",
              "content":"%s"
            }
          ]
        }
        """.formatted(
                modelName,
                userMessage
        );

        String aiResponse =
                webClientBuilder.build()
                        .post()
                        .uri(aiBaseUrl + "/v1/chat/completions")
                        .header("Content-Type", "application/json")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.ASSISTANT)
                        .content(aiResponse)
                        .build()
        );

        return aiResponse;
    }

    public String testChat(String message) {

        String body = """
        {
          "model":"%s",
          "messages":[
            {
              "role":"user",
              "content":"%s"
            }
          ]
        }
        """.formatted(
                modelName,
                message
        );

        return webClientBuilder.build()
                .post()
                .uri(aiBaseUrl + "/v1/chat/completions")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}