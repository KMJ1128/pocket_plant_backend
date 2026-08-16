package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글(boardId)의 댓글 목록 조회
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);

    // ⭐️ [신규 추가] 특정 게시글(boardId)의 총 댓글 개수 계산 (COUNT 쿼리)
    long countByBoardId(Long boardId);
}