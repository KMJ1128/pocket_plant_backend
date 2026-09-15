package com.pocket_plant.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import com.pocket_plant.backend.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {

            Long userId = jwtTokenProvider.getUserId(token);
            String tokenAuthVersion = jwtTokenProvider.getAuthVersion(token);
            boolean matchesCurrentAccount = tokenAuthVersion != null
                    && userRepository.findById(userId)
                    .map(user -> tokenAuthVersion.equals(user.getAuthVersion()))
                    .orElse(false);

            if (!matchesCurrentAccount) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication auth =
                    jwtTokenProvider.getAuthentication(token);

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Authorization: Bearer xxx
     */
    private String resolveToken(HttpServletRequest request) {

        String bearer =
                request.getHeader("Authorization");

        if (bearer != null &&
                bearer.startsWith("Bearer ")) {

            return bearer.substring(7);
        }

        return null;
    }
}
