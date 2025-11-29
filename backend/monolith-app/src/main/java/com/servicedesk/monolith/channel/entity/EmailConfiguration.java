package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfiguration extends BaseEntity {

    @Column(name = "channel_id", nullable = false, unique = true)
    private String channelId;

    @Column(nullable = false)
    private String name;

    @Column(name = "email_address", nullable = false, unique = true)
    private String emailAddress;

    // IMAP Configuration (for receiving)
    @Column(name = "imap_host")
    private String imapHost;

    @Column(name = "imap_port")
    private Integer imapPort = 993;

    @Column(name = "imap_username")
    private String imapUsername;

    @Column(name = "imap_password")
    private String imapPassword;

    @Column(name = "imap_ssl")
    private Boolean imapSsl = true;

    @Column(name = "imap_folder")
    private String imapFolder = "INBOX";

    // SMTP Configuration (for sending)
    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort = 587;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password")
    private String smtpPassword;

    @Column(name = "smtp_ssl")
    private Boolean smtpSsl = false;

    @Column(name = "smtp_tls")
    private Boolean smtpTls = true;

    @Column(name = "smtp_auth")
    private Boolean smtpAuth = true;

    // Display settings
    @Column(name = "from_name")
    private String fromName;

    @Column(name = "reply_to")
    private String replyTo;

    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;

    // Polling settings
    @Column(name = "poll_interval_seconds")
    private Integer pollIntervalSeconds = 60;

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

    @Column(name = "last_poll_at")
    private java.time.LocalDateTime lastPollAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
}
