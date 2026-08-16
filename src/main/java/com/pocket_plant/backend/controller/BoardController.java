package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.dto.board.BoardCreateRequest;
import com.pocket_plant.backend.dto.board.BoardResponseDto;
import com.pocket_plant.backend.entity.Board;
import com.pocket_plant.backend.entity.User;
import com.pocket_plant.backend.repository.BoardRepository;
import com.pocket_plant.backend.repository.CommentRepository;
import com.pocket_plant.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<BoardResponseDto>> getAllBoards() {
        List<BoardResponseDto> response =
                boardRepository
                        .findAllByOrderByIdDesc()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<BoardResponseDto> getBoard(
            @PathVariable Long id
    ) {
        Board board = findBoard(id);

        board.setViews(
                (board.getViews() == null ? 0 : board.getViews()) + 1
        );

        return ResponseEntity.ok(toResponse(board));
    }

    @GetMapping("/my")
    @Transactional(readOnly = true)
    public ResponseEntity<List<BoardResponseDto>> getMyBoards(
            Authentication authentication
    ) {
        String userId =
                String.valueOf(currentUserId(authentication));

        return ResponseEntity.ok(
                boardRepository
                        .findByUserIdOrderByIdDesc(userId)
                        .stream()
                        .map(this::toResponse)
                        .toList()
        );
    }

    @PostMapping
    @Transactional
    public ResponseEntity<BoardResponseDto> createBoard(
            Authentication authentication,
            @RequestBody BoardCreateRequest request
    ) {
        User user = currentUser(authentication);

        validateRequest(request);

        Board board = Board.builder()
                .title(request.getTitle().trim())
                .content(request.getContent().trim())
                .category(request.getCategory().trim())
                .userId(String.valueOf(user.getId()))
                .writer(user.getNickname())
                .views(0)
                .imageUris(cleanImages(request.getImageUris()))
                .build();

        return ResponseEntity.ok(
                toResponse(boardRepository.save(board))
        );
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<BoardResponseDto> updateBoard(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody BoardCreateRequest request
    ) {
        Board board = findBoard(id);

        verifyOwner(authentication, board);
        validateRequest(request);

        board.setTitle(request.getTitle().trim());
        board.setContent(request.getContent().trim());
        board.setCategory(request.getCategory().trim());

        board.getImageUris().clear();
        board.getImageUris().addAll(
                cleanImages(request.getImageUris())
        );

        return ResponseEntity.ok(toResponse(board));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteBoard(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Board board = findBoard(id);
        verifyOwner(authentication, board);

        commentRepository.deleteByBoardId(id);
        boardRepository.delete(board);

        return ResponseEntity.noContent().build();
    }

    private BoardResponseDto toResponse(Board board) {
        return new BoardResponseDto(
                board,
                commentRepository.countByBoardId(board.getId())
        );
    }

    private Board findBoard(Long id) {
        return boardRepository.findById(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "게시글을 찾을 수 없습니다."
                        )
                );
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

    private void verifyOwner(
            Authentication authentication,
            Board board
    ) {
        String currentUserId =
                String.valueOf(currentUserId(authentication));

        if (!currentUserId.equals(board.getUserId())) {
            throw new IllegalStateException(
                    "본인의 게시글만 변경할 수 있습니다."
            );
        }
    }

    private void validateRequest(BoardCreateRequest request) {
        if (
                request.getTitle() == null
                        || request.getTitle().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "제목은 필수입니다."
            );
        }

        if (
                request.getContent() == null
                        || request.getContent().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "내용은 필수입니다."
            );
        }

        if (
                request.getCategory() == null
                        || request.getCategory().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "카테고리는 필수입니다."
            );
        }
    }

    private List<String> cleanImages(
            List<String> images
    ) {
        if (images == null) {
            return new ArrayList<>();
        }

        return images.stream()
                .filter(value ->
                        value != null
                                && !value.isBlank()
                )
                .map(String::trim)
                .limit(5)
                .collect(
                        Collectors.toCollection(
                                ArrayList::new
                        )
                );
    }
}