package com.pocket_plant.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();

    public void sendVerificationCode(String email) {
        String code = String.valueOf(new Random().nextInt(899999) + 100000);

        verificationStorage.put(email, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(mailFrom);
        message.setSubject("회원가입 인증번호입니다.");
        message.setText("인증번호: " + code);

        mailSender.send(message);
    }

    public boolean verifyCode(String email, String inputCode) {
        String savedCode = verificationStorage.get(email);
        if (savedCode != null && savedCode.equals(inputCode)) {
            verificationStorage.remove(email);
            return true;
        }
        return false;
    }
}