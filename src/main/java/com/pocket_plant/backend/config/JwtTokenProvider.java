package com.pocket_plant.backend.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKeyString;

    @Value("${jwt.expiration-time}")
    private long validityInMilliseconds;

    private Key secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Keys.hmacShaKeyFor(secretKeyString.getBytes());
    }

    /** 토큰 생성 메서드 - JJWT 0.12.3 신규 API */
    public String createToken(Long userId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .subject(userId.toString())           // ✅ setSubject() 대신 subject()
                .issuedAt(now)                         // ✅ setIssuedAt() 대신 issuedAt()
                .expiration(validity)                  // ✅ setExpiration() 대신 expiration()
                .signWith(secretKey)                   // ✅ signWith(key, algo) 대신 signWith(key)
                .compact();
    }

    /** 토큰에서 인증 정보(Authentication) 객체 가져오기 */
    public Authentication getAuthentication(String token) {
        Long userId = getUserId(token);              // ✅ Integer → Long

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(userId, "", authorities);
    }

    /** 토큰에서 사용자 ID (Long) 추출 */
    public Long getUserId(String token) {
        return Long.valueOf(Jwts.parser()
                .setSigningKey(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    /** 토큰 유효성 검사 */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return !claims.getPayload().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}