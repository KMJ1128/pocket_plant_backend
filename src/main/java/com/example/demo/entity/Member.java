package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 번호를 알아서 인덱싱 해줌
    private Long id;

    @Column(unique = true, nullable = false)
    private String email; // 아이디(이메일) 역할

    @Column(nullable = false)
    private String password; // 비밀번호

    private String name; // 유저 이름 또는 닉네임

    private String role; // 권한 (일반유저, 운영진)

    private Boolean isEmailVerified = false; // 이메일 인증 여부

}
