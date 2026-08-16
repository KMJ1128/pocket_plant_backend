package com.pocket_plant.backend.controller;

import com.pocket_plant.backend.config.JwtTokenProvider;
import com.pocket_plant.backend.dto.board.BoardCreateRequest;
import com.pocket_plant.backend.dto.board.BoardResponseDto;
import com.pocket_plant.backend.entity.Board;
import com.pocket_plant.backend.repository.BoardRepository;
import com.pocket_plant.backend.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository; // ⭐️ 댓글 개수 조회를 위해 추가
    private final JwtTokenProvider jwtTokenProvider;

    // 생성자 주입
    public BoardController(BoardRepository boardRepository, CommentRepository commentRepository, JwtTokenProvider jwtTokenProvider) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 게시글 생성 API
     */
    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody BoardCreateRequest request){
        log.info("게시글 저장 시작 -> title: {}", request.getTitle());

        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .userId(request.getUserId())
                .writer(request.getWriter())
                .views(0) // 초기 조회수 0 설정
                .build();

        Board savedBoard = boardRepository.save(board);
        return ResponseEntity.ok(savedBoard);
    }

    /**
     * 1. 게시글 전체 목록 조회 API (조회수 + 댓글수 포함 반환)
     */
    @GetMapping
    public ResponseEntity<List<BoardResponseDto>> getAllBoards(){
        List<Board> boards = boardRepository.findAllByOrderByIdDesc();

        // ⭐️ 각 게시글 엔티티를 조회수와 댓글 수가 포함된 BoardResponseDto로 변환
        List<BoardResponseDto> responseDtos = boards.stream().map(board -> {
            long commentCount = commentRepository.countByBoardId(board.getId());
            return new BoardResponseDto(board, commentCount);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
    }

    /**
     * ⭐️ [신규 추가] 2. 게시글 단건 상세 조회 API (조회수 +1 자동 증가)
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoardDetail(@PathVariable Long id) {
        return boardRepository.findById(id).map(board -> {

            // Step 1: 조회수 1 증가 처리
            int currentViews = (board.getViews() != null) ? board.getViews() : 0;
            board.setViews(currentViews + 1);
            Board updatedBoard = boardRepository.save(board); // DB 업데이트

            // Step 2: 해당 게시글의 댓글 개수 조회
            long commentCount = commentRepository.countByBoardId(id);

            // Step 3: DTO로 변환하여 응답
            return ResponseEntity.ok(new BoardResponseDto(updatedBoard, commentCount));

        }).orElse(ResponseEntity.notFound().build()); // 게시글이 없으면 404
    }

    /**
     * 게시글 수정 API
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBoard(
            @PathVariable Long id,
            @RequestBody Board boardDetails,
            @RequestHeader(value = "Authorization", required = false) String bearerToken
    ){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 누락되었습니다.");
        }

        String token = bearerToken.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        Long currentUserId = jwtTokenProvider.getUserId(token);

        return boardRepository.findById(id).map(board -> {
            if (!String.valueOf(board.getUserId()).equals(String.valueOf(currentUserId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 게시글만 수정할 수 있습니다.");
            }

            board.setTitle(boardDetails.getTitle());
            board.setContent(boardDetails.getContent());
            board.setCategory(boardDetails.getCategory());

            Board updatedBoard = boardRepository.save(board);
            return ResponseEntity.ok(updatedBoard);

        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * 게시글 삭제 API
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String bearerToken
    ){
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 토큰이 누락되었습니다.");
        }

        String token = bearerToken.substring(7);

        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        Long currentUserId = jwtTokenProvider.getUserId(token);

        return boardRepository.findById(id).map(board -> {
            if (!String.valueOf(board.getUserId()).equals(String.valueOf(currentUserId))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("본인의 게시글만 삭제할 수 있습니다.");
            }

            boardRepository.delete(board);
            return ResponseEntity.noContent().build();

        }).orElse(ResponseEntity.notFound().build());
    }
}