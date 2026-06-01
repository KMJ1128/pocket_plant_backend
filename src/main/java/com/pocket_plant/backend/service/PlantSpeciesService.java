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
import tools.jackson.databind.JsonNode;

import java.io.IOException;
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

    @Value("${plant-api.perenual.url}")
    private String perenualUrl;
    @Value("${plant-api.perenual.key}")
    private String perenualKey;

    public Map<String, Object> identifyAndGetCareGuide(MultipartFile image) throws IOException {

        // --- [STEP 1] Pl@ntNet API로 이미지 전송해서 식물 학명 알아내기 ---
        String scientificName = getPlantNameFromImage(image);

        if (scientificName == null) {
            throw new RuntimeException("식물을 인식할 수 없습니다.");
        }

        // --- [STEP 2] Perenual API에 학명 검색해서 키우는 방법 가져오기 ---
        JsonNode careGuide = getCareGuideFromPerenual(scientificName);

        // --- [STEP 3] 결과 합치기 ---
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("plantName", scientificName);
        finalResponse.put("careGuide", careGuide);

        return finalResponse;
    }

    // 1단계: 이미지 분석 메서드
    private String getPlantNameFromImage(MultipartFile image) throws IOException {
        String url = plantNetUrl + "?api-key=" + plantNetKey;

        // Multipart 요청 구성 (이미지와 부위 지정)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        // 파일 데이터를 ByteArrayResource로 변환하여 첨부
        ByteArrayResource fileResource = new ByteArrayResource(image.getBytes()) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename();
            }
        };

        body.add("images", fileResource);
        body.add("organs", "leaf"); // 실내 관엽식물이므로 보통 '잎(leaf)'을 기준으로 분석

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Pl@ntNet 호출
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, requestEntity, JsonNode.class);

        // 응답에서 가장 확률이 높은 첫 번째 식물의 학명(Scientific Name) 추출
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode results = response.getBody().path("results");
            if (results.isArray() && results.size() > 0) {
                return results.get(0).path("species").path("scientificNameWithoutAuthor").asText();
            }
        }
        return null;
    }

    // 2단계: 데이터 조회 메서드
    private JsonNode getCareGuideFromPerenual(String plantName) {
        // 검색어에 띄어쓰기가 있을 수 있으므로 url 파라미터로 조합
        String url = perenualUrl + "?key=" + perenualKey + "&q=" + plantName;

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);

        // 검색 결과 중 첫 번째 식물 데이터 반환 (실무에서는 ID를 뽑아 상세 API를 한 번 더 호출할 수도 있습니다)
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            JsonNode data = response.getBody().path("data");
            if (data.isArray() && data.size() > 0) {
                return data.get(0); // 물 주기, 햇빛 등 기본 정보가 담긴 JSON 노드
            }
        }
        return null;
    }
}