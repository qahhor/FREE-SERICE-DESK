package com.servicedesk.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "telegram_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramConfiguration extends BaseEntity {

    @Column(name = "channel_id", nullable = false, unique = true)
    private String channelId;

    @Column(nullable = false)
    private String name;

    @Column(name = "bot_token", nullable = false)
    private String botToken;

    @Column(name = "bot_username")
    private String botUsername;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "webhook_secret")
    private String webhookSecret;

    @Column(name = "use_webhook")
    private Boolean useWebhook = false;

    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    @Column(name = "auto_create_ticket")
    private Boolean autoCreateTicket = true;

    @Column(name = "default_priority")
    private String defaultPriority = "MEDIUM";

    @Column(name = "default_team_id")
    private String defaultTeamId;

    @Column(name = "default_category_id")
    private String defaultCategoryId;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "last_update_id")
    private Long lastUpdateId;
}
