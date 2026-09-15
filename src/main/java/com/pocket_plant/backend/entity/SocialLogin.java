package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "social_logins",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_social_logins_provider_social_id",
                columnNames = {"provider", "social_id"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLogin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String provider;     // kakao, naver 등

    @Column(name = "social_id", nullable = false)
    private String socialId;     // 소셜 고유 ID
    private String accessToken;  // 🔑 이 필드가 꼭 있어야 Builder에서 accessToken() 호출 가능
    private String refresh_token;
    private String token_expiry;
    private String created_at;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
