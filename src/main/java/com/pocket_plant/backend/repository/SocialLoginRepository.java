package com.pocket_plant.backend.repository;


import com.pocket_plant.backend.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;




public interface SocialLoginRepository extends JpaRepository<SocialLogin, Integer> {

    Optional<SocialLogin> findByProviderAndSocialId(String provider, String socialId);
}

