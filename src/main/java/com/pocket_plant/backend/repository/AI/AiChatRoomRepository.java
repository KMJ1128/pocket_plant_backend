package com.pocket_plant.backend.repository.AI;

import com.pocket_plant.backend.entity.AI.AiChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiChatRoomRepository
        extends JpaRepository<AiChatRoom, Long> {

    Optional<AiChatRoom> findByIdAndUserId(
            Long id,
            Long userId
    );
}