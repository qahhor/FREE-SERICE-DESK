package com.servicedesk.channel.repository;

import com.servicedesk.channel.entity.EmailConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailConfigurationRepository extends JpaRepository<EmailConfiguration, String> {

    Optional<EmailConfiguration> findByChannelId(String channelId);

    Optional<EmailConfiguration> findByEmailAddress(String emailAddress);

    List<EmailConfiguration> findByEnabledTrue();

    @Query("SELECT ec FROM EmailConfiguration ec WHERE ec.enabled = true AND ec.imapHost IS NOT NULL")
    List<EmailConfiguration> findActiveInboundConfigurations();

    boolean existsByEmailAddress(String emailAddress);
}
