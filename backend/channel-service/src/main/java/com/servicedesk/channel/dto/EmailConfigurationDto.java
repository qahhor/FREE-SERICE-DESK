package com.servicedesk.channel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConfigurationDto {
    private String id;
    private String channelId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    private String emailAddress;

    // IMAP Configuration
    private String imapHost;
    private Integer imapPort;
    private String imapUsername;
    private String imapPassword;
    private Boolean imapSsl;
    private String imapFolder;

    // SMTP Configuration
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private Boolean smtpSsl;
    private Boolean smtpTls;
    private Boolean smtpAuth;

    // Display settings
    private String fromName;
    private String replyTo;
    private String signature;

    // Polling settings
    private Integer pollIntervalSeconds;
    private Boolean autoCreateTicket;
    private String defaultPriority;
    private String defaultTeamId;
    private String defaultCategoryId;

    private Boolean enabled;
}
