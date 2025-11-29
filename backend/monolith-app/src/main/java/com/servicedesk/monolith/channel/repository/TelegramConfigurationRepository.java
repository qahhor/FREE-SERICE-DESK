package com.servicedesk.monolith.channel.repository;

import com.servicedesk.monolith.channel.entity.TelegramConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TelegramConfigurationRepository extends JpaRepository<TelegramConfiguration, String> {

    Optional<TelegramConfiguration> findByChannelId(String channelId);

    Optional<TelegramConfiguration> findByBotUsername(String botUsername);

    List<TelegramConfiguration> findByEnabledTrue();

    List<TelegramConfiguration> findByUseWebhookFalseAndEnabledTrue();

    boolean existsByBotToken(String botToken);
}
