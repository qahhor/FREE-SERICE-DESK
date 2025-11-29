package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.WhatsAppConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhatsAppConfigurationRepository extends JpaRepository<WhatsAppConfiguration, String> {

    Optional<WhatsAppConfiguration> findByChannelId(String channelId);

    Optional<WhatsAppConfiguration> findByPhoneNumberId(String phoneNumberId);

    List<WhatsAppConfiguration> findByEnabledTrue();

    boolean existsByPhoneNumberId(String phoneNumberId);

    boolean existsByChannelId(String channelId);
}
