package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.board.CommentRequestDto;
import com.pocket_plant.backend.dto.board.CommentResponseDto;
import com.pocket_plant.backend.entity.Comment;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.BoardRepository;
import com.pocket_plant.backend.repository.CommentRepository;
import com.pocket_plant.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @GetMapping("/board/{boardId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long boardId
    ) {
        return ResponseEntity.ok(
                commentRepository
                        .findByBoardIdOrderByCreatedAtAsc(boardId)
                        .stream()
                        .map(CommentResponseDto::new)
                        .toList()
        );
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<List<CommentResponseDto>> getMyComments(
            Authentication authentication
    ) {
        String userId =
                String.valueOf(currentUserId(authentication));

        return ResponseEntity.ok(
                commentRepository
                        .findByUserIdOrderByCreatedAtDesc(userId)
                        .stream()
                        .map(CommentResponseDto::new)
                        .toList()
        );
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CommentResponseDto> createComment(
            Authentication authentication,
            @RequestBody CommentRequestDto request
    ) {
        if (
                request.getContent() == null
                        || request.getContent().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "댓글 내용을 입력해주세요."
            );
        }

        if (!boardRepository.existsById(request.getBoardId())) {
            throw new IllegalArgumentException(
                    "게시글을 찾을 수 없습니다."
            );
        }

        User user = currentUser(authentication);

        Comment parent = null;

        if (request.getParentId() != null) {
            parent = commentRepository
                    .findById(request.getParentId())
                    .orElseThrow(
                            () -> new IllegalArgumentException(
                                    "부모 댓글을 찾을 수 없습니다."
                            )
                    );

            if (!parent.getBoardId().equals(request.getBoardId())) {
                throw new IllegalArgumentException(
                        "다른 게시글의 댓글에는 답글을 작성할 수 없습니다."
                );
            }
        }

        Comment comment = Comment.builder()
                .boardId(request.getBoardId())
                .userId(String.valueOf(user.getId()))
                .writer(user.getNickname())
                .content(request.getContent().trim())
                .parent(parent)
                .build();

        return ResponseEntity.ok(
                new CommentResponseDto(
                        commentRepository.save(comment)
                )
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteComment(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Comment comment = commentRepository
                .findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "댓글을 찾을 수 없습니다."
                        )
                );

        String userId =
                String.valueOf(currentUserId(authentication));

        if (!userId.equals(comment.getUserId())) {
            throw new IllegalStateException(
                    "본인의 댓글만 삭제할 수 있습니다."
            );
        }

        commentRepository.delete(comment);

        return ResponseEntity.noContent().build();
    }

    private User currentUser(Authentication authentication) {
        return userRepository
                .findById(currentUserId(authentication))
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }

    private Long currentUserId(Authentication authentication) {
        if (
                authentication == null
                        || !(authentication.getPrincipal() instanceof Long)
        ) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        return (Long) authentication.getPrincipal();
    }
}