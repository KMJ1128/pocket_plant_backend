package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.MemberJoinRequest;
import com.pocket_plant.backend.dto.MemberTokenResponse;
import com.pocket_plant.backend.entity.MsgEntity;
import com.pocket_plant.backend.service.EmailService;
import com.pocket_plant.backend.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailApiController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberService memberService;


    Logger logger = LoggerFactory.getLogger(EmailApiController.class);


    @PostMapping("/verify-request")
    public String sendCode(@RequestParam("email") String email) {
        try {

            logger.info("이메일 인증 요청: {}", email);
            emailService.sendVerificationCode(email);
            return "인증번호가 발송되었습니다. 메일함을 확인해주세요!";
        } catch (Exception e) {
            return "메일 발송 중 오류가 발생했습니다.";
        }
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam("email") String email,
                             @RequestParam("code") String code) {
        logger.info("인증번호 검증 요청: email={}, code={}", email, code);

        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return "인증에 성공했습니다!";
        } else {
            return "인증번호가 틀렸거나 만료되었습니다.";
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody MemberJoinRequest request) {
        try {

            memberService.save(request);
            return ResponseEntity.ok("가입이 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<MsgEntity> login(@RequestBody MemberJoinRequest request) {
        try {
            logger.info("로그인 시도: {}", request.getEmail());
            MemberTokenResponse body = memberService.login(request);

            return ResponseEntity.ok(new MsgEntity("로그인 성공", body));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(new MsgEntity(e.getMessage(), null));
        }
    }
}