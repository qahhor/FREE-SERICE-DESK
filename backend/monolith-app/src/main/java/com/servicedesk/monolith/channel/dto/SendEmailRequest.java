package com.servicedesk.monolith.channel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendEmailRequest {

    @NotBlank(message = "Channel ID is required")
    private String channelId;

    private String ticketId;

    @NotEmpty(message = "At least one recipient is required")
    private List<@Email String> to;

    private List<@Email String> cc;
    private List<@Email String> bcc;

    @NotBlank(message = "Subject is required")
    private String subject;

    private String bodyText;
    private String bodyHtml;

    private String inReplyTo;

    private List<AttachmentDto> attachments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentDto {
        private String fileName;
        private String contentType;
        private String content; // Base64 encoded
    }
}
