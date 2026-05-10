package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    //임시로 담아둘 메모리
    private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();

    // 인증번호 생성 및 발송
    public void sendVerificationCode(String email) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000); // 6자리 랜덤 번호
        
        verificationStorage.put(email, code); // 메모리에 적어둠

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);

        message.setFrom("bwj917@naver.com");

        message.setSubject("회원가입 인증번호입니다.");
        message.setText("인증번호: " + code);

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String inputCode) {
        String savedCode = verificationStorage.get(email);
        return savedCode != null && savedCode.equals(inputCode);
    }
}