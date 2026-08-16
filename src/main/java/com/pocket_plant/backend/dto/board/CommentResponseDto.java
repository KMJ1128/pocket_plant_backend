package com.pocket_plant.backend.dto.board;

import com.pocket_plant.backend.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private Long id;
    private Long boardId;
    private String userId;
    private String writer;
    private String content;
    private LocalDateTime createdAt;
    private Long parentId; // 프론트엔드가 일반 댓글과 대댓글을 구분할 수 있도록 parentId 포함

    // Entity ➔ DTO 변환 생성자
    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.boardId = comment.getBoardId();
        this.userId = comment.getUserId();
        this.writer = comment.getWriter();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        // 부모 댓글이 존재하면 부모 ID 추출, 없으면 null 리턴
        this.parentId = (comment.getParent() != null) ? comment.getParent().getId() : null;
    }
}