package com.pocket_plant.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Autowired
    private JavaMailSender mailSender;

    private final Map<String, String> verificationStorage = new ConcurrentHashMap<>();
    private final Map<String, Boolean> verifiedEmailStorage = new ConcurrentHashMap<>();
    private final Logger log = LoggerFactory.getLogger(EmailService.class);
    public void sendVerificationCode(String email) {
        try {
            String code = String.valueOf(new Random().nextInt(899999) + 100000);

            verificationStorage.put(email, code);
            verifiedEmailStorage.remove(email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setFrom(mailFrom);
            message.setSubject("회원가입 인증번호입니다.");
            message.setText("인증번호: " + code);

            mailSender.send(message);

            logger.info("인증번호 생성: email={}, code={}", email, code);
        } catch (Exception e) {
            logger.error("이메일 발송 실패: email={}, error={}", email, e.getMessage());
            throw e;
        }
    }

    public boolean verifyCode(String email, String inputCode) {
        String savedCode = verificationStorage.get(email);

        logger.info("인증번호 검증: email={}, inputCode={}, savedCode={}", email, inputCode, savedCode);

        if (savedCode != null && savedCode.equals(inputCode)) {
            verificationStorage.remove(email);
            verifiedEmailStorage.put(email, true);
            return true;
        }
        return false;
    }

    public boolean isEmailVerified(String email) {
        return Boolean.TRUE.equals(verifiedEmailStorage.get(email));
    }

    public void consumeVerifiedEmail(String email) {
        verifiedEmailStorage.remove(email);
    }
}