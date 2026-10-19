package com.pocket_plant.backend.service;

import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final JavaMailSender mail = mock(JavaMailSender.class);
    private final Clock clock = mock(Clock.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final String email = "plant@example.com";
    private final Instant now = Instant.parse("2026-10-19T00:00:00Z");
    private PasswordResetService service;
    private User user;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(now);
        user = User.builder().email(email).password(encoder.encode("OldPass1!")).build();
        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        service = new PasswordResetService(users, encoder, mail, clock);
    }

    private String requestCode() {
        service.requestCode(email);
        ArgumentCaptor<SimpleMailMessage> sent = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mail, atLeastOnce()).send(sent.capture());
        String text = sent.getValue().getText();
        return text.substring(text.indexOf(": ") + 2, text.indexOf('\n'));
    }

    @Test
    void verifiedCodeChangesPasswordRevokesOldSessionsAndCannotBeReused() {
        String oldVersion = user.getAuthVersion();
        String code = requestCode();
        service.verifyCode(email, code);
        service.resetPassword(email, code, "NewPass2!");
        assertTrue(encoder.matches("NewPass2!", user.getPassword()));
        assertFalse(encoder.matches("OldPass1!", user.getPassword()));
        assertNotEquals(oldVersion, user.getAuthVersion());
        verify(users).saveAndFlush(user);
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, code, "OtherPass3!"));
        verify(users, times(1)).saveAndFlush(any());
    }

    @Test
    void codeMustBeVerifiedAndBelongToTheSameEmail() {
        String code = requestCode();
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, code, "NewPass2!"));
        service.verifyCode(email, code);
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword("other@example.com", code, "NewPass2!"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void verifiedCodeExpiresAfterTenMinutes() {
        String code = requestCode();
        service.verifyCode(email, code);
        when(clock.instant()).thenReturn(now.plusSeconds(600));
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, code, "NewPass2!"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void fiveWrongAttemptsInvalidateEvenTheCorrectCode() {
        String code = requestCode();
        String wrong = code.equals("100000") ? "100001" : "100000";
        for (int i = 0; i < 5; i++) {
            assertThrows(IllegalArgumentException.class, () -> service.verifyCode(email, wrong));
        }
        assertThrows(IllegalArgumentException.class, () -> service.verifyCode(email, code));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void resendRequiresANewVerificationAndHasACooldown() {
        String code = requestCode();
        service.verifyCode(email, code);
        assertThrows(IllegalArgumentException.class, () -> service.requestCode(email));
        when(clock.instant()).thenReturn(now.plusSeconds(61));
        String replacement = requestCode();
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, replacement, "NewPass2!"));
        service.verifyCode(email, replacement);
        service.resetPassword(email, replacement, "NewPass2!");
    }

    @Test
    void missingAndSocialAccountsDoNotReceiveResetCodes() {
        when(users.findByEmail(email)).thenReturn(Optional.empty());
        service.requestCode(email);
        user.setLoginType(User.LoginType.KAKAO);
        when(users.findByEmail(email)).thenReturn(Optional.of(user));
        service.requestCode(email);
        verifyNoInteractions(mail);
    }

    @Test
    void failedResendInvalidatesThePreviousVerifiedCode() {
        String code = requestCode();
        service.verifyCode(email, code);
        when(clock.instant()).thenReturn(now.plusSeconds(61));
        doThrow(new org.springframework.mail.MailSendException("offline")).when(mail).send(any(SimpleMailMessage.class));
        assertThrows(org.springframework.mail.MailSendException.class, () -> service.requestCode(email));
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, code, "NewPass2!"));
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void weakPasswordDoesNotChangeTheAccount() {
        String code = requestCode();
        service.verifyCode(email, code);
        assertThrows(IllegalArgumentException.class, () -> service.resetPassword(email, code, "weak"));
        assertTrue(encoder.matches("OldPass1!", user.getPassword()));
        verify(users, never()).saveAndFlush(any());
    }
}
