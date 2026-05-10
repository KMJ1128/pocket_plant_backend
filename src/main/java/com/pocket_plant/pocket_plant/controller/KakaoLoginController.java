package com.pocket_plant.pocket_plant.controller;

import com.pocket_plant.pocket_plant.dto.KakaoTokenRequest;
import com.pocket_plant.pocket_plant.dto.MemberTokenResponse;
import com.pocket_plant.pocket_plant.entity.MsgEntity;
import com.pocket_plant.pocket_plant.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 모바일 앱용 카카오 로그인 API
 * 앱에서 직접 받은 카카오 Access Token을 처리합니다
 */
@RestController
@RequestMapping("/kakao")
public class KakaoLoginController {

    @Autowired
    private KakaoService kaKaoService;

    /**
     * [핵심 엔드포인트] 모바일 앱에서 받은 카카오 Access Token으로 로그인
     * @param request 카카오 Access Token을 포함하는 요청
     * @return JWT 토큰과 사용자 정보
     */
    @PostMapping("/login/token")
    public ResponseEntity<MsgEntity> loginWithKakaoToken(@RequestBody KakaoTokenRequest request) {
        try {
            MemberTokenResponse tokenResponse = kaKaoService.loginWithToken(request.getAccessToken());
            return ResponseEntity.ok(new MsgEntity("로그인 성공", tokenResponse));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MsgEntity("로그인 실패", e.getMessage()));
        }
    }
}
