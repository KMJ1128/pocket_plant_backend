package com.pocket_plant.backend.dto.board;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequestDto {

    private Long boardId;
    private Long parentId;
    private String content;
}