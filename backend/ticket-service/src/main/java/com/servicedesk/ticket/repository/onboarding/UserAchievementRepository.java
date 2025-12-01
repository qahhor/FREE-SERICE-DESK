package com.servicedesk.ticket.repository.onboarding;

import com.servicedesk.ticket.entity.onboarding.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {

    List<UserAchievement> findByUserId(UUID userId);

    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(UUID userId);

    @Query("SELECT ua FROM UserAchievement ua WHERE ua.userId = :userId AND ua.achievement.achievementId = :achievementId")
    Optional<UserAchievement> findByUserIdAndAchievementId(
            @Param("userId") UUID userId,
            @Param("achievementId") String achievementId
    );

    @Query("SELECT COUNT(ua) FROM UserAchievement ua WHERE ua.userId = :userId")
    long countByUserId(@Param("userId") UUID userId);

    boolean existsByUserIdAndAchievement_AchievementId(UUID userId, String achievementId);
}
