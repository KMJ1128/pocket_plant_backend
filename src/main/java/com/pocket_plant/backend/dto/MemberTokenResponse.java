package com.pocket_plant.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberTokenResponse {

    private String serviceToken; // 우리 서비스가 발행한 인증 토큰 (예: JWT)
    private Long userId; // 로그인된 회원의 고유 ID
    private String nickname; // 로그인된 회원의 닉네임
    private String email; // 로그인된 회원의 이메일
    private String profileImageUrl; // 로그인된 회원의 프로필 이미지 URL
    private String kakaoId; // 로그인된 회원의 카카오 고유 ID
    private String naverId; // 로그인된 회원의 네이버 고유 ID
}
