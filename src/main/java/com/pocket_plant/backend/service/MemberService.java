package com.pocket_plant.backend.service;



import com.pocket_plant.backend.service.EmailService;
import com.pocket_plant.backend.config.JwtTokenProvider;
import com.pocket_plant.backend.dto.MemberJoinRequest;
import com.pocket_plant.backend.dto.MemberTokenResponse;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;


@Service
public class MemberService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;


    Logger logger = Logger.getLogger(MemberService.class.getName());

    public MemberService(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JwtTokenProvider jwtTokenProvider,
                         EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.emailService = emailService;
    }

    public void save(MemberJoinRequest dto) {


        if (!emailService.isEmailVerified(dto.getEmail())) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }

        userRepository.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        });
        userRepository.findByNickname(dto.getNickname()).ifPresent(u -> {
            throw new IllegalStateException("이미 사용 중인 닉네임입니다.");
        });
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setRole("ROLE_USER");
        user.setLoginType(User.LoginType.GENERAL);
        user.setIsEmailVerified(true);
        userRepository.save(user);
        emailService.consumeVerifiedEmail(dto.getEmail());
    }


    public MemberTokenResponse login(MemberJoinRequest dto) {
        String email = dto.getEmail();
        String rawPassword = dto.getPassword();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("가입되지 않은 이메일입니다."));
        if (user.getPassword() == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        String token = jwtTokenProvider.createToken(user.getId());
        return new MemberTokenResponse(
                token,
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getProfileImage(),
                user.getKakaoId(),
                user.getNaverId()
        );
    }
}