package com.pocket_plant.backend.service.AI;


import com.pocket_plant.backend.entity.AI.AiChatMessage;
import com.pocket_plant.backend.entity.AI.AiChatRoom;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.repository.AI.AiChatMessageRepository;
import com.pocket_plant.backend.repository.AI.AiChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final WebClient.Builder webClientBuilder;

    private final AiChatRoomRepository roomRepository;

    private final AiChatMessageRepository messageRepository;

    private final ObjectMapper objectMapper;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.model}")
    private String modelName;

    public String sendMessage(
            AiChatRoom room,
            String userMessage
    ) {

        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.USER)
                        .content(userMessage)
                        .build()
        );

        Plant plant =
                room.getPlant();

        String answer =
                requestAi(
                        userMessage,
                        plant
                );

        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.ASSISTANT)
                        .content(answer)
                        .build()
        );

        return answer;
    }

    public String testChat(
            String message
    ) {

        String prompt =
                buildDefaultSystemPrompt()
                        + """

                        사용자 질문:
                        %s
                        """.formatted(message);

        Map<String, Object> body =
                Map.of(
                        "model",
                        modelName,

                        "messages",
                        List.of(
                                Map.of(
                                        "role",
                                        "user",
                                        "content",
                                        prompt
                                )
                        ),

                        "temperature",
                        0.5,

                        "max_tokens",
                        128
                );

        String rawResponse =
                callAiServer(body);

        return extractAnswer(rawResponse);
    }

    private String requestAi(
            String userMessage,
            Plant plant
    ) {

        String systemPrompt =
                buildSystemPrompt(plant);

        String finalPrompt =
                systemPrompt
                        + """

                        사용자 질문:
                        %s
                        """.formatted(userMessage);

        Map<String, Object> body =
                Map.of(
                        "model",
                        modelName,

                        "messages",
                        List.of(
                                Map.of(
                                        "role",
                                        "user",
                                        "content",
                                        finalPrompt
                                )
                        ),

                        "temperature",
                        0.5,

                        "max_tokens",
                        128
                );

        String rawResponse =
                callAiServer(body);

        return extractAnswer(rawResponse);
    }

    private String callAiServer(
            Map<String, Object> body
    ) {

        try {
            System.out.println(
                    "AI 요청 body = "
                            + objectMapper.writeValueAsString(body)
            );

            return webClientBuilder
                    .baseUrl(aiBaseUrl)
                    .build()
                    .post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            System.out.println(
                    "AI 서버 응답 오류 status = "
                            + e.getStatusCode()
            );

            System.out.println(
                    "AI 서버 응답 body = "
                            + e.getResponseBodyAsString()
            );

            throw new RuntimeException(
                    "AI 서버 응답 오류: status="
                            + e.getStatusCode()
                            + ", body="
                            + e.getResponseBodyAsString(),
                    e
            );

        } catch (Exception e) {
            e.printStackTrace();

            throw new RuntimeException(
                    "AI 서버 호출 실패: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String extractAnswer(
            String rawResponse
    ) {

        try {
            JsonNode root =
                    objectMapper.readTree(
                            rawResponse
                    );

            JsonNode contentNode =
                    root
                            .path("choices")
                            .path(0)
                            .path("message")
                            .path("content");

            if (
                    !contentNode.isMissingNode()
                            &&
                            !contentNode.isNull()
            ) {
                return contentNode.asText();
            }

            return rawResponse;

        } catch (Exception e) {
            return rawResponse;
        }
    }

    private String buildSystemPrompt(
            Plant plant
    ) {

        String defaultPrompt =
                buildDefaultSystemPrompt();

        if (plant == null) {
            return defaultPrompt
                    + """

                    현재 연결된 식물 정보가 없습니다.
                    사용자가 식물 정보를 묻는 경우, 먼저 식물을 등록하거나 선택해 달라고 안내하세요.
                    """;
        }

        String plantName =
                safeText(
                        plant.getName(),
                        "이름 없는 식물"
                );

        String species =
                safeText(
                        plant.getSpecies(),
                        "종 정보 없음"
                );

        String personality =
                safeText(
                        plant.getPersonality(),
                        "차분하고 친절한 성격"
                );

        return defaultPrompt
                + """

                현재 대화하는 식물 정보:
                - 식물 이름: %s
                - 식물 종류: %s
                - 식물 성격: %s

                대화 스타일:
                - 너는 '%s'라는 식물 캐릭터처럼 말한다.
                - 식물의 성격은 '%s'이다.
                - 사용자가 식물과 대화하는 느낌을 받을 수 있게 답한다.
                - 식물 관리 정보는 정확하고 실용적으로 말한다.
                - 실제 센서 데이터가 없으면 현재 온도, 습도, 흙 상태를 아는 척하지 않는다.
                - 병충해나 질병은 단정하지 말고 가능성과 확인 방법을 알려준다.
                - 답변은 한국어로 한다.
                - 답변은 2~4문장 정도로 짧고 자연스럽게 한다.
                """.formatted(
                plantName,
                species,
                personality,
                plantName,
                personality
        );
    }

    private String buildDefaultSystemPrompt() {

        return """
                너는 Pocket Plant 앱의 AI 식물 도우미다.

                반드시 지켜야 할 규칙:
                - 한국어로 답한다.
                - 사용자의 반려식물 관리, 물주기, 햇빛, 온도, 습도, 흙 상태에 대해 도와준다.
                - 식물과 대화하는 느낌으로 친근하게 답한다.
                - 모르는 정보는 지어내지 않는다.
                - 센서 데이터가 주어지지 않았으면 현재 환경 상태를 아는 척하지 않는다.
                - 병충해나 질병은 단정하지 말고 확인 방법을 알려준다.
                - 답변은 짧게 한다.
                """;
    }

    private String safeText(
            String value,
            String defaultValue
    ) {

        if (
                value == null
                        ||
                        value.isBlank()
        ) {
            return defaultValue;
        }

        return value;
    }
}