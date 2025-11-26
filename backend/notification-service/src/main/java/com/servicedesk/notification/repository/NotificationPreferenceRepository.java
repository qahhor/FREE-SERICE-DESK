package com.servicedesk.notification.repository;

import com.servicedesk.notification.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {

    Optional<NotificationPreference> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
