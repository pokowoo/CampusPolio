package com.campuspolio.domain.profile.repository;

import com.campuspolio.domain.profile.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    boolean existsByUserId(Long userId);

    Optional<UserProfile> findByUserId(Long userId);
}