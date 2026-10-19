package com.pocket_plant.backend.service.AI;


import com.pocket_plant.backend.dto.AI.Chat.ChatResponse;
import com.pocket_plant.backend.entity.AI.AiChatMessage;
import com.pocket_plant.backend.entity.AI.AiChatRoom;
import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.repository.AI.AiChatMessageRepository;
import com.pocket_plant.backend.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final WebClient.Builder webClientBuilder;

    private final AiChatMessageRepository messageRepository;

    private final SensorDataRepository sensorDataRepository;

    private final ObjectMapper objectMapper;

    @Value("${ai.base-url}")
    private String aiBaseUrl;

    @Value("${ai.model}")
    private String modelName;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    public ChatResponse sendMessage(
            AiChatRoom room,
            String userMessage
    ) {

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("메시지를 입력해주세요.");
        }

        String normalizedMessage = userMessage.trim();
        if (normalizedMessage.length() > 1000) {
            throw new IllegalArgumentException("메시지는 1000자 이하여야 합니다.");
        }

        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.USER)
                        .content(normalizedMessage)
                        .build()
        );

        Plant plant =
                room.getPlant();

        LocalDateTime generatedAt = LocalDateTime.now();
        LocalDateTime sensorPeriodStart =
                generatedAt.minusDays(SensorContextFactory.PERIOD_DAYS);
        ChatResponse.SensorContext sensorContext = SensorContextFactory.create(
                sensorDataRepository.findTopByPlantIdOrderByRegDateDesc(plant.getId()).orElse(null),
                sensorDataRepository.summarizeSince(plant.getId(), sensorPeriodStart),
                generatedAt
        );

        String answer = requestAi(room, plant, sensorContext);

        messageRepository.save(
                AiChatMessage.builder()
                        .room(room)
                        .sender(AiChatMessage.SenderType.ASSISTANT)
                        .content(answer)
                        .build()
        );

        return new ChatResponse(answer, plant.getId(), generatedAt, sensorContext);
    }

    public String testChat(
            String message
    ) {

        Map<String, Object> body =
                Map.of(
                        "model",
                        modelName,
                        "messages",
                        List.of(
                                Map.of(
                                        "role", "system",
                                        "content", buildDefaultSystemPrompt()
                                ),
                                Map.of(
                                        "role", "user",
                                        "content", message
                                )
                        ),
                        "temperature",
                        0.5,
                        "max_completion_tokens",
                        256,
                        "response_format",
                        responseFormat()
                );

        String rawResponse =
                callAiServer(body);

        return extractStructuredAnswer(rawResponse);
    }

    private String requestAi(
            AiChatRoom room,
            Plant plant,
            ChatResponse.SensorContext sensorContext
    ) {
        String systemPrompt = buildSystemPrompt(plant, sensorContext);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        List<AiChatMessage> recentMessages =
                new ArrayList<>(messageRepository.findTop12ByRoomIdOrderByIdDesc(room.getId()));
        Collections.reverse(recentMessages);
        for (AiChatMessage message : recentMessages) {
            messages.add(Map.of(
                    "role",
                    message.getSender() == AiChatMessage.SenderType.USER ? "user" : "assistant",
                    "content",
                    message.getContent()
            ));
        }

        Map<String, Object> body =
                Map.of(
                        "model", modelName,
                        "messages", messages,
                        "temperature", 0.5,
                        "max_completion_tokens", 256,
                        "response_format", responseFormat()
                );

        String rawResponse =
                callAiServer(body);

        return extractStructuredAnswer(rawResponse);
    }

    private String callAiServer(
            Map<String, Object> body
    ) {

        try {
            return webClientBuilder
                    .baseUrl(aiBaseUrl)
                    .defaultHeader(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + openAiApiKey
                    )
                    .build()
                    .post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

        } catch (WebClientResponseException e) {
            System.err.println("OpenAI 응답 오류 status = " + e.getStatusCode());

            throw new RuntimeException(
                    "AI 응답을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.",
                    e
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.",
                    e
            );
        }
    }

    private String extractStructuredAnswer(
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
                JsonNode answerNode = objectMapper.readTree(contentNode.asText()).path("answer");
                if (!answerNode.isMissingNode() && !answerNode.asText().isBlank()) {
                    return answerNode.asText();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("AI 응답 형식을 해석하지 못했습니다.", e);
        }

        throw new RuntimeException("AI가 답변을 반환하지 않았습니다.");
    }

    private String buildSystemPrompt(
            Plant plant,
            ChatResponse.SensorContext sensorContext
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

        String sensorJson;
        try {
            sensorJson = objectMapper.writeValueAsString(sensorContext);
        } catch (Exception e) {
            throw new RuntimeException("센서 정보를 준비하지 못했습니다.", e);
        }

        return defaultPrompt
                + """

                현재 대화하는 식물 정보:
                - 식물 이름: %s
                - 식물 종류: %s
                - 식물 성격: %s

                서버가 DB에서 확정한 센서 정보(JSON):
                %s

                대화 스타일:
                - 너는 '%s'라는 식물 캐릭터처럼 말한다.
                - 식물의 성격은 '%s'이다.
                - 사용자가 식물과 대화하는 느낌을 받을 수 있게 답한다.
                - 식물 관리 정보는 정확하고 실용적으로 말한다.
                - 센서 질문에는 반드시 위 JSON만 근거로 답한다. 수치나 측정 시각을 만들지 않는다.
                - available=false이면 측정값이 없다고 솔직하게 말한다.
                - stale=true이면 오래된 측정값임을 밝히고 현재 상태처럼 단정하지 않는다.
                - latest는 가장 최근 값이고 recent는 최근 7일의 평균/최저/최고다.
                - temperatureCelsius는 섭씨, airHumidityPercent는 %%, lightRaw와 soilMoistureRaw는 기기가 보낸 원시값이다.
                - 토양 수분 원시값은 센서 보정/단위 정보가 없으므로 숫자만으로 물이 필요하다고 확정하지 않는다.
                - 병충해나 질병은 단정하지 말고 가능성과 확인 방법을 알려준다.
                - 답변은 한국어로 한다.
                - 답변은 2~4문장 정도로 짧고 자연스럽게 한다.
                - 출력은 지정된 JSON 스키마에 맞는 answer 하나만 반환한다.
                """.formatted(
                plantName,
                species,
                personality,
                sensorJson,
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
                - 출력은 지정된 JSON 스키마에 맞는 answer 하나만 반환한다.
                """;
    }

    private Map<String, Object> responseFormat() {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "answer", Map.of(
                                "type", "string",
                                "minLength", 1
                        )
                ),
                "required", List.of("answer"),
                "additionalProperties", false
        );

        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "pocket_plant_chat_answer",
                        "strict", true,
                        "schema", schema
                )
        );
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
