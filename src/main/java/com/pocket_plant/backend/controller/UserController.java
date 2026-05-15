package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.UserDTO;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public UserDTO me(Authentication authentication) {

        Long userId =
                (Long) authentication.getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow();

        return UserDTO.fromEntity(user);
    }
}