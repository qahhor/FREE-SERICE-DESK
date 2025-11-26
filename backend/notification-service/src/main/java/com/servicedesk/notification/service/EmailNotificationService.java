package com.servicedesk.notification.service;

import com.servicedesk.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Async
    public void sendNotificationEmail(Notification notification) {
        try {
            // In production, fetch user email from user service
            String userEmail = getUserEmail(notification.getUserId());

            if (userEmail == null) {
                log.warn("No email found for user: {}", notification.getUserId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("title", notification.getTitle());
            context.setVariable("message", notification.getMessage());
            context.setVariable("actionUrl", notification.getActionUrl());
            context.setVariable("type", notification.getType().name());

            String htmlContent = templateEngine.process("notification-email", context);

            helper.setTo(userEmail);
            helper.setSubject("[ServiceDesk] " + notification.getTitle());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Notification email sent to: {}", userEmail);

        } catch (Exception e) {
            log.error("Failed to send notification email: {}", e.getMessage(), e);
        }
    }

    private String getUserEmail(String userId) {
        // TODO: Call user service to get email
        // For now, return null to skip email sending in development
        return null;
    }
}
