package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.config.JwtTokenProvider;
import com.pocket_plant.backend.dto.board.CommentRequestDto;
import com.pocket_plant.backend.dto.board.CommentResponseDto;
import com.pocket_plant.backend.entity.Comment;
import com.pocket_plant.backend.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final JwtTokenProvider jwtTokenProvider; // 토큰 검증기 주입

    public CommentController(CommentRepository commentRepository, JwtTokenProvider jwtTokenProvider) {
        this.commentRepository = commentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 1. 댓글 / 대댓글 등록 API (POST /api/comments)
     */
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentRequestDto requestDto) {
        log.info("댓글 작성 요청 -> boardId: {}, parentId: {}", requestDto.getBoardId(), requestDto.getParentId());

        // 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .boardId(requestDto.getBoardId())
                .userId(requestDto.getUserId())
                .writer(requestDto.getWriter())
                .content(requestDto.getContent())
                .build();

        // ⭐️ parentId가 전달되었다면 DB에서 부모 댓글을 찾아 대댓글 관계로 연결
        if (requestDto.getParentId() != null) {
            Comment parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다. id=" + requestDto.getParentId()));
            comment.setParent(parentComment);
        }

        // DB에 저장
        Comment savedComment = commentRepository.save(comment);

        // 안전한 Response DTO로 변환하여 응답
        return ResponseEntity.ok(new CommentResponseDto(savedComment));
    }

    /**
     * 2. 특정 게시글의 전체 댓글/대댓글 목록 조회 API (GET /api/comments/board/{boardId})
     */
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByBoard(@PathVariable Long boardId) {
        List<Comment> comments = commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);

        // 엔티티 리스트를 DTO 리스트로 변환하여 반환
        List<CommentResponseDto> responseDtos = comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * 3. 댓글 / 대댓글 삭제 API (DELETE /api/comments/{id})
     * JWT 토큰을 통한 작성자 본인 검증 포함
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String bearerToken
    ) {
        // Step 1: Authorization Header 토큰 검증
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 누락되었습니다.");
        }

        String token = bearerToken.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        // Step 2: 토큰에서 진짜 삭제 요청 유저 ID 추출
        Long currentUserId = jwtTokenProvider.getUserId(token);

        // Step 3: DB에서 삭제할 댓글 조회
        return commentRepository.findById(id).map(comment -> {

            // ⭐️ [보안 검증] 댓글 작성자와 삭제 요청자가 일치하는지 확인
            if (!String.valueOf(comment.getUserId()).equals(String.valueOf(currentUserId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 댓글만 삭제할 수 있습니다.");
            }

            // Step 4: 검증 통과 시 삭제 (부모 댓글 삭제 시 Cascade에 의해 대댓글도 함께 삭제)
            commentRepository.delete(comment);
            log.info("댓글 삭제 성공 -> commentId: {}", id);

            return ResponseEntity.noContent().build(); // 204 No Content

        }).orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }
}