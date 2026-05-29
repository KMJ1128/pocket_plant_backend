package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.UserDTO;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDTO me(Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow();

        logger.info("현재 로그인한 사용자 정보: ID={}, 이메일={}, 닉네임={}", user.getId(), user.getEmail(), user.getNickname());
        return UserDTO.fromEntity(user);
    }
}