package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="social_logins")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String provider;     // kakao, naver 등
    private String socialId;     // 소셜 고유 ID
    private String accessToken;  // 🔑 이 필드가 꼭 있어야 Builder에서 accessToken() 호출 가능
    private String refresh_token;
    private String token_expiry;
    private String created_at;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
