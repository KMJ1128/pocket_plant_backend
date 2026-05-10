package com.pocket_plant.pocket_plant.service;

import com.pocket_plant.pocket_plant.client.KakaoClient;
import com.pocket_plant.pocket_plant.dto.MemberTokenResponse;
import com.pocket_plant.pocket_plant.entity.User;
import com.pocket_plant.pocket_plant.repository.UserRepository;
import com.pocket_plant.pocket_plant.security.JwtProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * 카카오 로그인 비즈니스 로직
 * - 카카오 토큰 검증
 * - 사용자 저장/업데이트
 * - JWT 토큰 발급
 */
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Value("${kakao.app.key}")
    private String kakaoAppKey;

    /**
     * 카카오 Access Token으로 로그인 처리
     * @param kakaoAccessToken 모바일 앱에서 받은 카카오 Access Token
     * @return 서비스 JWT 토큰과 사용자 정보
     */
    public MemberTokenResponse loginWithToken(String kakaoAccessToken) throws Exception {
        // 1️⃣ 카카오 API에서 검증 (카카오 ID만 필요)
        Map<String, Object> kakaoUserInfo = kakaoClient.getUserInfo(kakaoAccessToken);
        
        // 2️⃣ 카카오 ID 추출 (이게 유일한 식별자)
        String kakaoId = kakaoUserInfo.get("id").toString();
        
        // 3️⃣ DB에서 이 카카오 ID가 이미 있는지 확인
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseGet(() -> {
                    // 없으면 새로 생성
                    User newUser = User.builder()
                            .kakaoId(kakaoId)
                            .loginType(User.LoginType.KAKAO)
                            .build();
                    return userRepository.save(newUser);
                });
        
        // 4️⃣ JWT 토큰 발급
        String serviceToken = jwtProvider.generateToken(user.getId());
        
        // 5️⃣ 응답 생성
        return MemberTokenResponse.builder()
                .serviceToken(serviceToken)
                .userId(Math.toIntExact(user.getId()))
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImage())
                .kakaoId(user.getKakaoId())
                .build();
    }
    
    /**
     * JWT 토큰 검증
     */
    public boolean validateToken(String token) {
        return jwtProvider.validateToken(token);
    }
    
    /**
     * JWT 토큰에서 User ID 추출
     */
    public Long getUserIdFromJWT(String token) {
        return jwtProvider.getUserIdFromJWT(token);
    }
    
    /**
     * User ID로 사용자 정보 조회
     */
    public Optional<MemberTokenResponse> getUserInfoById(Long userId) {
        return userRepository.findById(userId)
                .map(user -> MemberTokenResponse.builder()
                        .serviceToken(null) // 조회 시에는 토큰 필요 없음
                        .userId(Math.toIntExact(user.getId()))
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .profileImageUrl(user.getProfileImage())
                        .kakaoId(user.getKakaoId())
                        .build());
    }
}
