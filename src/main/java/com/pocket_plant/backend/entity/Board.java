package com.pocket_plant.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String writer;

    @Builder.Default
    @Column(nullable = false)
    private Integer views = 0;

    @ElementCollection
    @CollectionTable(
            name = "board_images",
            joinColumns = @JoinColumn(name = "board_id")
    )
    @OrderColumn(name = "sort_order")
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> imageUris = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (views == null) {
            views = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}