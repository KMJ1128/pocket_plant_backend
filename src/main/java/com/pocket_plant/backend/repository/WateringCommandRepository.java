package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.WateringCommand;
import com.pocket_plant.backend.entity.WateringCommandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WateringCommandRepository extends JpaRepository<WateringCommand, Long> {
    boolean existsByPlantIdAndStatusIn(Long plantId, Collection<WateringCommandStatus> statuses);

    List<WateringCommand> findByPlantIdAndStatusIn(
            Long plantId,
            Collection<WateringCommandStatus> statuses
    );

    Optional<WateringCommand> findFirstByPlantIdAndStatusAndExpiresAtAfterOrderByCreatedAtAsc(
            Long plantId,
            WateringCommandStatus status,
            LocalDateTime now
    );

    Optional<WateringCommand> findFirstByPlantIdAndStatusAndDispatchedAtBeforeAndExpiresAtAfterOrderByCreatedAtAsc(
            Long plantId,
            WateringCommandStatus status,
            LocalDateTime retryBefore,
            LocalDateTime now
    );

    List<WateringCommand> findTop50ByPlantIdOrderByCreatedAtDesc(Long plantId);
}
