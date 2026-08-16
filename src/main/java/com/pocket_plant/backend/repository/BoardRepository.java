package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository
        extends JpaRepository<Board, Long> {

    List<Board> findAllByOrderByIdDesc();

    List<Board> findByUserIdOrderByIdDesc(
            String userId
    );
}