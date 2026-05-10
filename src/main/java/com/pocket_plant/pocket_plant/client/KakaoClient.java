package com.pocket_plant.pocket_plant.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 카카오 API 호출 클래스 (간단 버전)
 * 모바일 앱에서 받은 Access Token을 검증합니다
 */
@Component
public class KakaoClient {
    
    @Value("${kakao.api.user-info-url:https://kapi.kakao.com/v2/user/me}")
    private String userInfoUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 카카오 Access Token으로 사용자 정보 조회
     * @param accessToken 모바일 앱에서 받은 카카오 Access Token
     * @return 카카오 사용자 정보 맵
     */
    public Map<String, Object> getUserInfo(String accessToken) throws Exception {
        try {
            // Authorization 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);
            
            // 카카오 API 호출
            Map<String, Object> response = restTemplate.postForObject(userInfoUrl, request, Map.class);
            
            if (response == null) {
                throw new IllegalArgumentException("카카오 응답이 비어있습니다.");
            }
            
            return response;
        } catch (Exception e) {
            throw new IllegalArgumentException("유효하지 않은 카카오 Access Token입니다.", e);
        }
    }
}



