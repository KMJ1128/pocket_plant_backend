package com.pocket_plant.backend.repository.AI;

import com.pocket_plant.backend.entity.AI.AiChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiChatRoomRepository
        extends JpaRepository<AiChatRoom, Long> {

    List<AiChatRoom> findByUserIdOrderByUpdatedAtDesc(Long userId);
}