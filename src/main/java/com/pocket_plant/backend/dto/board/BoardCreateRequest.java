package com.pocket_plant.backend.dto.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest {
    private String title; // 제목
    private String content; // 작성 내용
    private String category; // 카테고리
    private String userId; // 작성자 id
    private String writer; // 작성자 이름(닉네임)
}
