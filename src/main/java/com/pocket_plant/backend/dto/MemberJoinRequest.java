package com.pocket_plant.backend.dto;

import lombok.Data;

@Data
public class MemberJoinRequest {
    private String email;
    private String password;
    private String nickname;
}