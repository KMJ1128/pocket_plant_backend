package com.pocket_plant.pocket_plant.dto;

import com.pocket_plant.pocket_plant.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String profileImage;
    private String kakaoId;
    private String naverId;
    private String loginType;
    private String role;
    
    /**
     * User 엔티티를 DTO로 변환
     */
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .kakaoId(user.getKakaoId())
                .naverId(user.getNaverId())
                .loginType(user.getLoginType().name())
                .build();
    }
}

