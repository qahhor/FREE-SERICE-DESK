package com.servicedesk.monolith.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Event for sending emails (replaces RabbitMQ email.queue)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private String templateName;
    private Map<String, Object> templateData;
    private List<String> attachments;
    private String priority;
}
