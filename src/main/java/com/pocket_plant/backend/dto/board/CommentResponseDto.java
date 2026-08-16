package com.pocket_plant.backend.dto.board;

import com.pocket_plant.backend.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {

    private final String id;
    private final String postId;
    private final String userId;
    private final String writer;
    private final String content;
    private final LocalDateTime date;
    private final String parentId;

    public CommentResponseDto(Comment comment) {
        id = String.valueOf(comment.getId());
        postId = String.valueOf(comment.getBoardId());
        userId = comment.getUserId();
        writer = comment.getWriter();
        content = comment.getContent();
        date = comment.getCreatedAt();

        parentId = comment.getParent() == null
                ? null
                : String.valueOf(comment.getParent().getId());
    }
}