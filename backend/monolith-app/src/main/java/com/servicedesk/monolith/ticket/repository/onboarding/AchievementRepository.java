package com.servicedesk.monolith.ticket.repository.onboarding;

import com.servicedesk.monolith.ticket.entity.onboarding.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    Optional<Achievement> findByAchievementId(String achievementId);

    List<Achievement> findByActiveTrue();

    List<Achievement> findByActiveTrueOrderByDisplayOrder();

    List<Achievement> findByType(Achievement.AchievementType type);

    boolean existsByAchievementId(String achievementId);
}
