package com.servicedesk.channel.dto;

import com.servicedesk.channel.entity.WhatsAppConfiguration;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppConfigurationDto {

    private String id;
    private String channelId;
    private String name;
    private String phoneNumberId;
    private String businessAccountId;
    private String displayPhoneNumber;
    private String webhookUrl;
    private String welcomeMessage;
    private Boolean autoCreateTicket;
    private String defaultPriority;
    private String defaultTeamId;
    private String defaultCategoryId;
    private Boolean enabled;
    private WhatsAppConfiguration.ApiVersion apiVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String channelId;
        private String name;
        private String phoneNumberId;
        private String businessAccountId;
        private String accessToken;
        private String verifyToken;
        private String appSecret;
        private String displayPhoneNumber;
        private String welcomeMessage;
        private Boolean autoCreateTicket;
        private String defaultPriority;
        private String defaultTeamId;
        private String defaultCategoryId;
        private WhatsAppConfiguration.ApiVersion apiVersion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String accessToken;
        private String verifyToken;
        private String appSecret;
        private String displayPhoneNumber;
        private String welcomeMessage;
        private Boolean autoCreateTicket;
        private String defaultPriority;
        private String defaultTeamId;
        private String defaultCategoryId;
        private Boolean enabled;
        private WhatsAppConfiguration.ApiVersion apiVersion;
    }
}
