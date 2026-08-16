package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository
        extends JpaRepository<Comment, Long> {

    List<Comment> findByBoardIdOrderByCreatedAtAsc(
            Long boardId
    );

    List<Comment> findByUserIdOrderByCreatedAtDesc(
            String userId
    );

    long countByBoardId(Long boardId);

    void deleteByBoardId(Long boardId);
}