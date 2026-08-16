package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 댓글 식별자 (PK)

    @Column(nullable = false)
    private Long boardId; // 댓글이 작성된 게시글 ID (FK 역할)

    @Column(nullable = false)
    private String userId; // 작성자 ID (유저 식별자)

    private String writer; // 작성자 닉네임/이름

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    private LocalDateTime createdAt; // 작성 일시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent; // 부모 댓글 객체

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> children = new ArrayList<>(); // 대댓글 리스트

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // DB에 저장되기 직전 현재 시간 자동 등록
    }
}