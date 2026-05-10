package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString // 내용을 문자열로 예쁘게 보여주는 기능
public class MemberJoinDTO {

    // 1. 아이디 (이메일)
    private String email;

    // 2. 인증번호
    private String verificationCode;

    // 3. 비밀번호
    private String password;

    // 4. 비밀번호 확인
    private String passwordConfirm;

    // 5. 이름
    private String name;
}