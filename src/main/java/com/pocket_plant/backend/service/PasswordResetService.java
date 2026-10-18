package com.pocket_plant.backend.service;

import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private static final Duration VALIDITY = Duration.ofMinutes(10);
    private static final String INVALID_CODE = "인증번호가 틀렸거나 만료되었습니다. 인증번호를 다시 요청해 주세요.";
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JavaMailSender mailSender;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Challenge> challenges = new HashMap<>();

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Autowired
    public PasswordResetService(UserRepository users, PasswordEncoder encoder, JavaMailSender mailSender) {
        this(users, encoder, mailSender, Clock.systemUTC());
    }

    PasswordResetService(UserRepository users, PasswordEncoder encoder, JavaMailSender mailSender, Clock clock) {
        this.users = users;
        this.encoder = encoder;
        this.mailSender = mailSender;
        this.clock = clock;
    }

    public synchronized void requestCode(String email) {
        Instant now = clock.instant();
        challenges.values().removeIf(challenge -> !now.isBefore(challenge.expiresAt));
        Challenge previous = challenges.get(email);
        if (previous != null && now.isBefore(previous.expiresAt.minus(VALIDITY).plusSeconds(60))) {
            throw new IllegalArgumentException("인증번호 재발송은 1분 뒤에 시도해 주세요.");
        }
        // Account existence and social sign-in status are not disclosed by this endpoint.
        if (users.findByEmail(email).filter(this::isGeneralAccount).isEmpty()) return;
        String code = String.valueOf(100000 + random.nextInt(900000));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("PocketPlant 비밀번호 재설정 인증번호");
        message.setText("인증번호: " + code + "\n10분 이내에 입력해 주세요. 요청하지 않았다면 이 메일을 무시하세요.");
        // A failed resend must not leave the previous authorization active.
        challenges.remove(email);
        mailSender.send(message);
        challenges.put(email, new Challenge(code, now.plus(VALIDITY)));
    }

    public synchronized void verifyCode(String email, String code) {
        requireChallenge(email, code).verified = true;
    }

    @Transactional
    public synchronized void resetPassword(String email, String code, String newPassword) {
        if (newPassword == null || !newPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,15}$")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수기호를 포함한 8~15자리여야 합니다.");
        }
        Challenge challenge = requireChallenge(email, code);
        if (!challenge.verified) throw new IllegalArgumentException("이메일 인증을 먼저 완료해 주세요.");
        User user = users.findByEmail(email).filter(this::isGeneralAccount)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_CODE));
        user.setPassword(encoder.encode(newPassword));
        // The existing JWT filter rejects sessions issued before this password reset.
        user.setAuthVersion(UUID.randomUUID().toString());
        users.saveAndFlush(user);
        challenges.remove(email);
    }

    private boolean isGeneralAccount(User user) {
        return user.getLoginType() == User.LoginType.GENERAL && user.getPassword() != null;
    }

    private Challenge requireChallenge(String email, String code) {
        Challenge challenge = challenges.get(email);
        if (challenge == null) throw new IllegalArgumentException(INVALID_CODE);
        if (!clock.instant().isBefore(challenge.expiresAt)) {
            challenges.remove(email);
            throw new IllegalArgumentException(INVALID_CODE);
        }
        if (!challenge.code.equals(code)) {
            if (++challenge.failedAttempts >= 5) challenges.remove(email);
            throw new IllegalArgumentException(INVALID_CODE);
        }
        return challenge;
    }

    private static class Challenge {
        final String code;
        final Instant expiresAt;
        int failedAttempts;
        boolean verified;

        Challenge(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
