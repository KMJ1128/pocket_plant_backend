package com.pocket_plant.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth
                        // 웹 정적 파일 및 프런트 화면
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/*.js",
                                "/*.css",
                                "/*.svg",
                                "/*.ico",
                                "/*.png",
                                "/uploads/**",
                                "/login",
                                "/signup",
                                "/forgot-password",
                                "/reset-password",
                                "/board",
                                "/board/**",
                                "/register",
                                "/error"
                        ).permitAll()

                        // 로그인·회원가입·이메일 인증
                        .requestMatchers(
                                "/kakao/**",
                                "/api/email/**",
                                "/api/auth/**"
                        ).permitAll()

                        // ESP32 센서 데이터 수신은 토큰 없이 허용
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/sensor"
                        ).permitAll()

                        // 식물 환경 정보 조회
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/plant/**"
                        ).permitAll()

                        // 게시판 및 댓글 조회만 공개
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/boards",
                                "/api/boards/**",
                                "/api/comments/board/**"
                        ).permitAll()

                        // AI 서버 연결 확인용
                        .requestMatchers(
                                "/api/ai-chat/test"
                        ).permitAll()

                        /*
                         * 나머지 요청은 JWT 필요:
                         * - 식물 등록·수정·삭제
                         * - 이미지 업로드
                         * - 센서 데이터 조회
                         * - AI 채팅
                         * - 질병 진단
                         * - 게시글 작성·수정·삭제
                         * - 댓글 작성·삭제
                         * - 사용자 정보
                         */
                        .anyRequest()
                        .authenticated()
                )

                .httpBasic(basic ->
                        basic.disable()
                )

                .formLogin(form ->
                        form.disable()
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtTokenProvider
                        ),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}