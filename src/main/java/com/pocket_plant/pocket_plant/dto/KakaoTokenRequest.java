package com.pocket_plant.pocket_plant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 모바일 앱에서 전송하는 카카오 Access Token 요청
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoTokenRequest {

    private String accessToken;
}

