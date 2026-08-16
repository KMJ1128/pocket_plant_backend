package com.pocket_plant.backend.dto.board;

import com.pocket_plant.backend.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponseDto {
    private Long id;            // 게시글 ID
    private String title;       // 제목
    private String content;     // 내용
    private String category;    // 카테고리
    private String userId;      // 작성자 ID
    private String writer;      // 작성자 닉네임
    private Integer views;      // ⭐️ 조회수
    private Long commentCount;  // ⭐️ 댓글 개수
    private LocalDateTime createdAt; // 작성일시

    // Board Entity와 댓글 수를 받아 DTO로 변환하는 생성자
    public BoardResponseDto(Board board, Long commentCount) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.category = board.getCategory();
        this.userId = board.getUserId();
        this.writer = board.getWriter();
        this.views = (board.getViews() != null) ? board.getViews() : 0;
        this.commentCount = (commentCount != null) ? commentCount : 0L;
        this.createdAt = board.getCreatedAt();
    }
}