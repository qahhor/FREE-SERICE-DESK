package com.servicedesk.monolith.channel.controller;

import com.servicedesk.monolith.channel.dto.EmailConfigurationDto;
import com.servicedesk.monolith.channel.dto.EmailConnectionTestResult;
import com.servicedesk.monolith.channel.dto.SendEmailRequest;
import com.servicedesk.monolith.channel.entity.EmailConfiguration;
import com.servicedesk.monolith.channel.entity.EmailMessage;
import com.servicedesk.monolith.channel.repository.EmailConfigurationRepository;
import com.servicedesk.monolith.channel.service.EmailService;
import com.servicedesk.common.dto.ApiResponse;
import com.servicedesk.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/channels/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailConfigurationRepository configRepository;
    private final EmailService emailService;

    @GetMapping("/configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EmailConfigurationDto>>> getAllConfigurations() {
        List<EmailConfigurationDto> configs = configRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/configurations/{channelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<EmailConfigurationDto>> getConfiguration(@PathVariable String channelId) {
        EmailConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Email configuration not found"));
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PostMapping("/configurations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailConfigurationDto>> createConfiguration(
            @Valid @RequestBody EmailConfigurationDto request) {
        if (configRepository.existsByEmailAddress(request.getEmailAddress())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email address already configured"));
        }

        EmailConfiguration config = EmailConfiguration.builder()
                .channelId(request.getChannelId())
                .name(request.getName())
                .emailAddress(request.getEmailAddress())
                .imapHost(request.getImapHost())
                .imapPort(request.getImapPort() != null ? request.getImapPort() : 993)
                .imapUsername(request.getImapUsername())
                .imapPassword(request.getImapPassword())
                .imapSsl(request.getImapSsl() != null ? request.getImapSsl() : true)
                .imapFolder(request.getImapFolder() != null ? request.getImapFolder() : "INBOX")
                .smtpHost(request.getSmtpHost())
                .smtpPort(request.getSmtpPort() != null ? request.getSmtpPort() : 587)
                .smtpUsername(request.getSmtpUsername())
                .smtpPassword(request.getSmtpPassword())
                .smtpSsl(request.getSmtpSsl() != null ? request.getSmtpSsl() : false)
                .smtpTls(request.getSmtpTls() != null ? request.getSmtpTls() : true)
                .smtpAuth(request.getSmtpAuth() != null ? request.getSmtpAuth() : true)
                .fromName(request.getFromName())
                .replyTo(request.getReplyTo())
                .signature(request.getSignature())
                .pollIntervalSeconds(request.getPollIntervalSeconds() != null ? request.getPollIntervalSeconds() : 60)
                .autoCreateTicket(request.getAutoCreateTicket() != null ? request.getAutoCreateTicket() : true)
                .defaultPriority(request.getDefaultPriority() != null ? request.getDefaultPriority() : "MEDIUM")
                .defaultTeamId(request.getDefaultTeamId())
                .defaultCategoryId(request.getDefaultCategoryId())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        config = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PutMapping("/configurations/{channelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailConfigurationDto>> updateConfiguration(
            @PathVariable String channelId,
            @Valid @RequestBody EmailConfigurationDto request) {
        EmailConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Email configuration not found"));

        if (request.getName() != null) config.setName(request.getName());
        if (request.getImapHost() != null) config.setImapHost(request.getImapHost());
        if (request.getImapPort() != null) config.setImapPort(request.getImapPort());
        if (request.getImapUsername() != null) config.setImapUsername(request.getImapUsername());
        if (request.getImapPassword() != null) config.setImapPassword(request.getImapPassword());
        if (request.getImapSsl() != null) config.setImapSsl(request.getImapSsl());
        if (request.getImapFolder() != null) config.setImapFolder(request.getImapFolder());
        if (request.getSmtpHost() != null) config.setSmtpHost(request.getSmtpHost());
        if (request.getSmtpPort() != null) config.setSmtpPort(request.getSmtpPort());
        if (request.getSmtpUsername() != null) config.setSmtpUsername(request.getSmtpUsername());
        if (request.getSmtpPassword() != null) config.setSmtpPassword(request.getSmtpPassword());
        if (request.getSmtpSsl() != null) config.setSmtpSsl(request.getSmtpSsl());
        if (request.getSmtpTls() != null) config.setSmtpTls(request.getSmtpTls());
        if (request.getSmtpAuth() != null) config.setSmtpAuth(request.getSmtpAuth());
        if (request.getFromName() != null) config.setFromName(request.getFromName());
        if (request.getReplyTo() != null) config.setReplyTo(request.getReplyTo());
        if (request.getSignature() != null) config.setSignature(request.getSignature());
        if (request.getPollIntervalSeconds() != null) config.setPollIntervalSeconds(request.getPollIntervalSeconds());
        if (request.getAutoCreateTicket() != null) config.setAutoCreateTicket(request.getAutoCreateTicket());
        if (request.getDefaultPriority() != null) config.setDefaultPriority(request.getDefaultPriority());
        if (request.getDefaultTeamId() != null) config.setDefaultTeamId(request.getDefaultTeamId());
        if (request.getDefaultCategoryId() != null) config.setDefaultCategoryId(request.getDefaultCategoryId());
        if (request.getEnabled() != null) config.setEnabled(request.getEnabled());

        config = configRepository.save(config);
        return ResponseEntity.ok(ApiResponse.success(toDto(config)));
    }

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<EmailMessage>> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailMessage message = emailService.sendEmail(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @GetMapping("/messages/ticket/{ticketId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<List<EmailMessage>>> getEmailsByTicket(@PathVariable String ticketId) {
        List<EmailMessage> messages = emailService.getEmailsByTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @PostMapping("/configurations/{channelId}/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EmailConnectionTestResult>> testConnection(@PathVariable String channelId) {
        EmailConfiguration config = configRepository.findByChannelId(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Email configuration not found"));

        EmailConnectionTestResult result = emailService.testConnection(config);
        
        // Always return success for the API call, with the test result containing detailed status
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private EmailConfigurationDto toDto(EmailConfiguration config) {
        return EmailConfigurationDto.builder()
                .id(config.getId())
                .channelId(config.getChannelId())
                .name(config.getName())
                .emailAddress(config.getEmailAddress())
                .imapHost(config.getImapHost())
                .imapPort(config.getImapPort())
                .imapSsl(config.getImapSsl())
                .imapFolder(config.getImapFolder())
                .smtpHost(config.getSmtpHost())
                .smtpPort(config.getSmtpPort())
                .smtpSsl(config.getSmtpSsl())
                .smtpTls(config.getSmtpTls())
                .smtpAuth(config.getSmtpAuth())
                .fromName(config.getFromName())
                .replyTo(config.getReplyTo())
                .signature(config.getSignature())
                .pollIntervalSeconds(config.getPollIntervalSeconds())
                .autoCreateTicket(config.getAutoCreateTicket())
                .defaultPriority(config.getDefaultPriority())
                .defaultTeamId(config.getDefaultTeamId())
                .defaultCategoryId(config.getDefaultCategoryId())
                .enabled(config.getEnabled())
                .build();
    }
}
