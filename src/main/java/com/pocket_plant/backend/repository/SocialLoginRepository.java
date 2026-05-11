package com.pocket_plant.backend.repository;


import com.pocket_plant.backend.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;




public interface SocialLoginRepository extends JpaRepository<SocialLogin, Integer> {

    // socialId로 SocialLogin 찾기 dd
    Optional<SocialLogin> findBySocialId(String socialId);
    Optional<SocialLogin> findByProviderAndSocialId(String provider, String socialId);
}

