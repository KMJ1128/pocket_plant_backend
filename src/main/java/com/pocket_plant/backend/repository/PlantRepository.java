package com.pocket_plant.backend.repository;

import com.pocket_plant.backend.entity.Plant;
import com.pocket_plant.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository
        extends JpaRepository<Plant, Long> {

    List<Plant> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    Optional<Plant> findByIdAndUserId(
            Long plantId,
            Long userId
    );

    Optional<Plant> findFirstByMacAddressOrderByIdDesc(
            String macAddress
    );

    Optional<Plant> findFirstByUserAndBookmarkedTrueOrderByCreatedAtDesc(
            User user
    );

    Optional<Plant> findFirstByUserOrderByCreatedAtDesc(
            User user
    );
}