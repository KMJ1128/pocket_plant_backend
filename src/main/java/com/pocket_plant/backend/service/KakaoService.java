package com.pocket_plant.backend.service;


import com.pocket_plant.backend.config.JwtTokenProvider;

import com.pocket_plant.backend.dto.KakaoDTO;
import com.pocket_plant.backend.dto.MemberTokenResponse;

import com.pocket_plant.backend.entity.SocialLogin;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.SocialLoginRepository;
import com.pocket_plant.backend.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;



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

    private static final Logger log = LoggerFactory.getLogger(KakaoService.class);


    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SocialLoginRepository socialLoginRepository;

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;



    public MemberTokenResponse loginWithToken(String kakaoAccessToken) throws Exception {


        KakaoDTO kakaoInfo = getUserInfoFromKakao(kakaoAccessToken);

        log.info("--- 카카오 로그인 사용자 정보 ---");
        log.info("카카오 로그인 사용자 정보: ID={}, 닉네임={}", kakaoInfo.getId(), kakaoInfo.getNickname());
        log.info("-----------------------------");

        String socialId = "kakao_" + kakaoInfo.getId();

        SocialLogin socialLogin = socialLoginRepository
                .findBySocialId(socialId)
                .orElse(null);

        User user;

        if (socialLogin != null) {
            user = socialLogin.getUser(); // 이미 존재하면 해당 User 가져오기
        } else {
            // ✅ [핵심 수정] 신규 회원 생성: nickname 필드를 Builder에서 제거하여 DB에 NULL로 저장되도록 합니다.
            String baseNickname = kakaoInfo.getNickname();

            user = User.builder()
                    .nickname(baseNickname) // 💡 카카오 닉네임은 username에 저장
                    .build();
            user = userRepository.save(user);

            // SocialLogin 기록 생성
            socialLoginRepository.save(SocialLogin.builder()
                    .user(user)
                    .provider("kakao")
                    .socialId(socialId)
                    .accessToken(kakaoAccessToken)
                    .build());
        }

        // 3. 우리 서비스 인증 토큰 발행
        String serviceToken = jwtTokenProvider.createToken(user.getId());


        // 4. 결과 반환
        return new MemberTokenResponse(
                serviceToken,
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage(),
                user.getKakaoId(),
                user.getNaverId()

        );
    }





    private KakaoDTO getUserInfoFromKakao(String kakaoAccessToken) {
        RestClient restClient = RestClient.create();

        RestClient.RequestHeadersUriSpec<?> uriSpec = restClient.get();

        RestClient.RequestHeadersSpec<?> headersSpec = uriSpec.uri("https://kapi.kakao.com/v2/user/me");

        return headersSpec
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new RuntimeException("Kakao Error: " + response.getStatusCode());
                })
                .body(KakaoDTO.class);
    }

}
