package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController // 이 부품은 HTML 페이지가 아니라 '데이터'를 응답으로 줍니다.
@RequestMapping("/api/email")
public class EmailApiController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/verify-request")
    public String sendCode(@RequestParam("email") String email) {
        try {
            emailService.sendVerificationCode(email);
            return "인증번호가 발송되었습니다. 메일함을 확인해주세요!";
        } catch (Exception e) {
            return "메일 발송 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("email") String email,
                             @RequestParam("code") String code) {
        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return "인증에 성공했습니다!";
        } else {
            return "인증번호가 틀렸거나 만료되었습니다.";
        }
    }
}