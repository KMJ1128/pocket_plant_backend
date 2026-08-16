package com.pocket_plant.backend.dto.board;

import com.pocket_plant.backend.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class BoardResponseDto {

    private final String id;
    private final String userId;
    private final String writer;
    private final String title;
    private final String content;
    private final String category;
    private final Integer views;
    private final Long commentsCount;
    private final LocalDateTime date;
    private final List<String> imageUris;
    private final String imageUri;

    public BoardResponseDto(
            Board board,
            Long commentsCount
    ) {
        this.id = String.valueOf(board.getId());
        this.userId = board.getUserId();
        this.writer = board.getWriter();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.category = board.getCategory();
        this.views = board.getViews() == null
                ? 0
                : board.getViews();
        this.commentsCount = commentsCount == null
                ? 0L
                : commentsCount;
        this.date = board.getCreatedAt();

        this.imageUris = board.getImageUris() == null
                ? new ArrayList<>()
                : new ArrayList<>(board.getImageUris());

        this.imageUri = imageUris.isEmpty()
                ? null
                : imageUris.get(0);
    }
}