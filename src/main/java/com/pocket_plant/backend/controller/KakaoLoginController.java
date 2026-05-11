package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.KakaoTokenRequest;
import com.pocket_plant.backend.dto.MemberTokenResponse;
import com.pocket_plant.backend.entity.MsgEntity;
import com.pocket_plant.backend.service.KakaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 모바일 앱용 카카오 로그인 API
 * 앱에서 직접 받은 카카오 Access Token을 처리합니다
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
public class KakaoLoginController {

    private final KakaoService kaKaoService;


    @PostMapping("/login/token")
    public ResponseEntity<MsgEntity> loginWithKakaoToken(@RequestBody KakaoTokenRequest request) throws Exception {

        // 서비스 로직: 카카오 토큰 검증, 회원가입/로그인 처리, 서비스 토큰 발행
        MemberTokenResponse response = kaKaoService.loginWithToken(request.getAccessToken());

        return ResponseEntity.ok()
                .body(new MsgEntity("로그인 성공", response));
    }
}
