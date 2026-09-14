package com.pocket_plant.backend.service;

import com.pocket_plant.backend.dto.DiseasePredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class PlantDiseaseService {


    @Value("${fast.api.url}")
    private String fastApiUrl;

    public DiseasePredictionResponse predictDisease(MultipartFile imageFile, String species) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename() != null ? imageFile.getOriginalFilename() : "image.jpg";
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", fileResource);
        body.add("species", species == null ? "" : species.trim());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 💡 주입받은 aiServerUrl 변수를 사용
        ResponseEntity<DiseasePredictionResponse> response;
        try {
            response = restTemplate.postForEntity(
                    fastApiUrl, requestEntity, DiseasePredictionResponse.class
            );
        } catch (ResourceAccessException e) {
            throw new DiseaseServerUnavailableException(
                    "로컬 진단 서버가 실행 중이 아닙니다. AI 서버를 먼저 실행해주세요.", e
            );
        } catch (HttpStatusCodeException e) {
            throw new DiseaseServerUnavailableException(
                    "로컬 진단 서버가 요청을 처리하지 못했습니다. 모델 파일과 서버 로그를 확인해주세요.", e
            );
        }

        if (response.getBody() == null) {
            throw new DiseaseServerUnavailableException("로컬 진단 서버가 빈 응답을 반환했습니다.");
        }

        return response.getBody();
    }

    public static class DiseaseServerUnavailableException extends RuntimeException {
        public DiseaseServerUnavailableException(String message) {
            super(message);
        }

        public DiseaseServerUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
