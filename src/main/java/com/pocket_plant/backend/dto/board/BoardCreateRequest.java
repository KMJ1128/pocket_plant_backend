package com.pocket_plant.backend.dto.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest {

    private String title;
    private String content;
    private String category;

    /*
     * userId와 writer는 프런트에서 받지 않습니다.
     * JWT 사용자 정보로 백엔드가 설정합니다.
     */
    private List<String> imageUris = new ArrayList<>();
}