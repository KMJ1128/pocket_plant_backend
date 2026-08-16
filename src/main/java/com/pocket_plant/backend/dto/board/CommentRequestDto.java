package com.pocket_plant.backend.dto.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {
    private Long boardId;  // 댓글을 달 게시글 ID
    private String userId; // 작성자 ID
    private String writer; // 작성자 닉네임
    private String content;// 댓글/대댓글 내용
    private Long parentId; // 부모 댓글 ID (일반 댓글은 null, 대댓글은 부모 댓글의 id)
}