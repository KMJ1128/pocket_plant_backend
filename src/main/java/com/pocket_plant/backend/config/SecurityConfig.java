package com.pocket_plant.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 모바일 앱용 API 기반 보안 설정
 * Stateless (JWT 토큰 기반) 방식
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (API는 토큰 기반이므로 필요 없음)
            .csrf(csrf -> csrf.disable())
            
            // Stateless 설정 (세션 미사용, JWT 토큰만 사용)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // API 엔드포인트 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 로그인 관련 엔드포인트는 공개
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/health").permitAll()
                
                // 나머지는 인증 필요
                .anyRequest().authenticated()
            )
            
            // HTTP Basic 비활성화 (JWT 토큰 사용)
            .httpBasic(basic -> basic.disable())
            
            // 기본 로그인 폼 비활성화
            .formLogin(login -> login.disable());
        
        return http.build();
    }
}

