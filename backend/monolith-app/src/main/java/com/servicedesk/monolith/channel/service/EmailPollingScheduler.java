package com.servicedesk.monolith.channel.service;

import com.servicedesk.monolith.channel.entity.EmailConfiguration;
import com.servicedesk.monolith.channel.repository.EmailConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailPollingScheduler {

    private final EmailConfigurationRepository configRepository;
    private final EmailService emailService;

    @Scheduled(fixedDelayString = "${servicedesk.email.poll-interval:30000}")
    public void pollEmails() {
        log.debug("Starting email polling...");

        List<EmailConfiguration> configs = configRepository.findActiveInboundConfigurations();

        for (EmailConfiguration config : configs) {
            if (shouldPoll(config)) {
                try {
                    log.debug("Polling emails for: {}", config.getEmailAddress());
                    emailService.fetchEmails(config);
                } catch (Exception e) {
                    log.error("Error polling emails for {}: {}", config.getEmailAddress(), e.getMessage());
                }
            }
        }

        log.debug("Email polling completed");
    }

    private boolean shouldPoll(EmailConfiguration config) {
        if (config.getLastPollAt() == null) {
            return true;
        }

        LocalDateTime nextPollTime = config.getLastPollAt()
                .plusSeconds(config.getPollIntervalSeconds());

        return LocalDateTime.now().isAfter(nextPollTime);
    }
}
