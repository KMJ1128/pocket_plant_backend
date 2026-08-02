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
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class PlantDiseaseService {


    @Value("${fast.api.url}")
    private String fastApiUrl;

    public DiseasePredictionResponse predictDisease(MultipartFile imageFile) throws IOException {
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

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 💡 주입받은 aiServerUrl 변수를 사용
        ResponseEntity<DiseasePredictionResponse> response = restTemplate.postForEntity(
                fastApiUrl, requestEntity, DiseasePredictionResponse.class
        );

        return response.getBody();
    }
}