package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService service;

    @PostMapping("/request")
    public String request(@Valid @RequestBody EmailRequest request) {
        service.requestCode(request.email().trim());
        return "가입된 일반 계정이라면 인증번호가 발송됩니다. 메일함을 확인해 주세요.";
    }

    @PostMapping("/verify")
    public String verify(@Valid @RequestBody CodeRequest request) {
        service.verifyCode(request.email().trim(), request.code());
        return "인증에 성공했습니다!";
    }

    @PostMapping("/confirm")
    public String confirm(@Valid @RequestBody ResetRequest request) {
        service.resetPassword(request.email().trim(), request.code(), request.newPassword());
        return "비밀번호가 변경되었습니다.";
    }

    public record EmailRequest(@NotBlank @Email String email) {}
    public record CodeRequest(@NotBlank @Email String email, @NotBlank @Pattern(regexp = "[0-9]{6}") String code) {}
    public record ResetRequest(@NotBlank @Email String email, @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
                               @NotBlank String newPassword) {}
}
