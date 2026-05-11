package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nickname;
    
    private String password; // 암호화 필요
    
    @Column(unique = true)
    private String email;

    private String profileImage;
    
    // 소셜 로그인용
    @Column(unique = true)
    private String kakaoId;
    
    @Column(unique = true)
    private String naverId;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoginType loginType = LoginType.GENERAL; // GENERAL, KAKAO, NAVER
    
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;
    
    private java.time.LocalDateTime updatedAt;
    
    // LoginType Enum
    public enum LoginType {
        GENERAL,  // 자체 회원가입
        KAKAO,    // 카카오 로그인
        NAVER     // 네이버 로그인
    }
    
    @PrePersist
    public void prePersist() {
        this.createdAt = java.time.LocalDateTime.now();
        this.updatedAt = java.time.LocalDateTime.now();
        if (this.loginType == null) {
            this.loginType = LoginType.GENERAL;
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}

