package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.KakaoTokenRequest;
import com.pocket_plant.backend.dto.MemberTokenResponse;
import com.pocket_plant.backend.entity.MsgEntity;
import com.pocket_plant.backend.service.KakaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    /**
     * JWT 토큰으로 사용자 정보 조회 (기존 회원용)
     */
    @GetMapping("/user/info")
    public ResponseEntity<MsgEntity> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            // Authorization 헤더에서 토큰 추출
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(new MsgEntity("인증 실패", "Invalid authorization header"));
            }

            String token = authHeader.substring(7);

            // JWT 토큰 검증
            if (!kaKaoService.validateToken(token)) {
                return ResponseEntity.badRequest()
                        .body(new MsgEntity("인증 실패", "Invalid or expired token"));
            }

            // 토큰에서 User ID 추출
            Long userId = kaKaoService.getUserIdFromJWT(token);

            // 사용자 정보 조회
            Optional<MemberTokenResponse> userOpt = kaKaoService.getUserInfoById(userId);

            if (userOpt.isPresent()) {
                MemberTokenResponse user = userOpt.get();
                return ResponseEntity.ok(new MsgEntity("사용자 정보 조회 성공", user));
            } else {
                return ResponseEntity.notFound()
                        .build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MsgEntity("사용자 정보 조회 실패", e.getMessage()));
        }
    }
}
