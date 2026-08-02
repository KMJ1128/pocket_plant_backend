package com.pocket_plant.backend.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/", "/index.html", "/assets/**",
                                "/*.js", "/*.css", "/*.svg", "/*.ico", "/*.png",
                                "/kakao/**",
                                "/api/email/**",
                                "/api/auth/**",
                                "/api/ai-chat/test",
                                "/api/sensor",
                                "/api/sensor/**",
                                "/api/plants/**",
                                "/login",
                                "/signup",
                                "/forgot-password",
                                "/reset-password",
                                "/board",
                                "/board/**",
                                "/register",
                                "/api/ai-chat/**",
                                "/error",
                                "/api/plant/**",
                                "/api/disease/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .httpBasic(basic -> basic.disable())

                .formLogin(form -> form.disable())

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}