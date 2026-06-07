package com.pocket_plant.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlantSpeciesService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${plant-api.plantnet.url}")
    private String plantNetUrl;

    @Value("${plant-api.plantnet.key}")
    private String plantNetKey;

    public Map<String, Object> identifyPlantImage(MultipartFile image) throws IOException {

        System.out.println("====== 이미지 분석 요청 시작 ======");

        String url = plantNetUrl + "?api-key=" + plantNetKey + "&lang=en";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        body.add("images", fileResource);
        body.add("organs", "auto");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode results = response.getBody().path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode bestMatch = results.get(0);

                String scientificName = bestMatch.path("species").path("scientificNameWithoutAuthor").asText();
                String koreanName = getKoreanNameFromWikipedia(scientificName);
                double score = Math.round(bestMatch.path("score").asDouble() * 1000) / 10.0;

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("koreanName", koreanName);
                resultMap.put("scientificName", scientificName);
                resultMap.put("accuracy", score + "%");

                System.out.println("AI 인식 완료! 추출된 한글 이름: " + koreanName + " (" + score + "%)");
                return resultMap;
            }
        }

        throw new RuntimeException("식물 인식 실패 또는 결과가 없습니다.");
    }

    private String getKoreanNameFromWikipedia(String scientificName) {
        try {
            URI wikiUri = UriComponentsBuilder.fromUriString("https://ko.wikipedia.org/w/api.php")
                    .queryParam("action", "query")
                    .queryParam("list", "search")
                    .queryParam("srsearch", scientificName)
                    .queryParam("utf8", "")
                    .queryParam("format", "json")
                    .build(false)
                    .toUri();

            // 💡 [핵심 추가 부분] 위키백과 봇 차단(403)을 피하기 위한 User-Agent 명찰 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "PocketPlantApp/1.0 (Contact: test@pocketplant.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // getForEntity 대신 exchange를 사용하여 헤더를 함께 전송
            ResponseEntity<JsonNode> response = restTemplate.exchange(wikiUri, HttpMethod.GET, entity, JsonNode.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode searchResults = response.getBody().path("query").path("search");

                if (searchResults.isArray() && searchResults.size() > 0) {
                    String title = searchResults.get(0).path("title").asText();

                    if (title.contains("(")) {
                        title = title.substring(0, title.indexOf("(")).trim();
                    }
                    return title;
                }
            }
        } catch (Exception e) {
            System.out.println("위키백과 변환 중 오류 (무시됨): " + e.getMessage());
        }

        return scientificName;
    }
}