package com.servicedesk.monolith.channel.entity;

import com.servicedesk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "whatsapp_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppConfiguration extends BaseEntity {

    @Column(name = "channel_id", nullable = false, unique = true)
    private String channelId;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number_id", nullable = false)
    private String phoneNumberId;

    @Column(name = "business_account_id", nullable = false)
    private String businessAccountId;

    @Column(name = "access_token", nullable = false, length = 500)
    private String accessToken;

    @Column(name = "verify_token", nullable = false)
    private String verifyToken;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "app_secret")
    private String appSecret;

    @Column(name = "display_phone_number")
    private String displayPhoneNumber;

    @Column(name = "welcome_message", columnDefinition = "TEXT")
    private String welcomeMessage;

    @Column(name = "auto_create_ticket")
    @Builder.Default
    private Boolean autoCreateTicket = true;

    @Column(name = "default_priority")
    @Builder.Default
    private String defaultPriority = "MEDIUM";

    @Column(name = "default_team_id")
    private String defaultTeamId;

    @Column(name = "default_category_id")
    private String defaultCategoryId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_version")
    @Builder.Default
    private ApiVersion apiVersion = ApiVersion.V18_0;

    public enum ApiVersion {
        V17_0("v17.0"),
        V18_0("v18.0"),
        V19_0("v19.0");

        private final String version;

        ApiVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}
