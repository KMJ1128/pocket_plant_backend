package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 PK (식별자)

    @Column(nullable = false)
    private String title; // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 게시글 내용

    private String category; // 게시글 카테고리

    @Column(nullable = false)
    private String userId; // 작성자 ID

    private String writer; // 작성자 닉네임

    //  조회수 필드 (기본값 0 설정)
    @Builder.Default
    @Column(columnDefinition = "integer default 0")
    private Integer views = 0;

    private LocalDateTime createdAt; // 작성 일시

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // DB 저장 직전 시간 자동 설정
        if (this.views == null) {
            this.views = 0; // views가 null인 경우 0으로 안전하게 초기화
        }
    }
}