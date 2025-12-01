package com.servicedesk.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for email connection test results
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailConnectionTestResult {

    private boolean success;
    private boolean imapSuccess;
    private boolean smtpSuccess;
    private String imapMessage;
    private String smtpMessage;
    private long imapResponseTimeMs;
    private long smtpResponseTimeMs;

    /**
     * Create a successful result
     */
    public static EmailConnectionTestResult success(String imapMessage, String smtpMessage,
                                                     long imapResponseTimeMs, long smtpResponseTimeMs) {
        return EmailConnectionTestResult.builder()
                .success(true)
                .imapSuccess(true)
                .smtpSuccess(true)
                .imapMessage(imapMessage)
                .smtpMessage(smtpMessage)
                .imapResponseTimeMs(imapResponseTimeMs)
                .smtpResponseTimeMs(smtpResponseTimeMs)
                .build();
    }

    /**
     * Create a partial result (one succeeded, one failed)
     */
    public static EmailConnectionTestResult partial(boolean imapSuccess, String imapMessage, long imapResponseTimeMs,
                                                     boolean smtpSuccess, String smtpMessage, long smtpResponseTimeMs) {
        return EmailConnectionTestResult.builder()
                .success(imapSuccess && smtpSuccess)
                .imapSuccess(imapSuccess)
                .smtpSuccess(smtpSuccess)
                .imapMessage(imapMessage)
                .smtpMessage(smtpMessage)
                .imapResponseTimeMs(imapResponseTimeMs)
                .smtpResponseTimeMs(smtpResponseTimeMs)
                .build();
    }

    /**
     * Create a failure result
     */
    public static EmailConnectionTestResult failure(String imapMessage, String smtpMessage) {
        return EmailConnectionTestResult.builder()
                .success(false)
                .imapSuccess(false)
                .smtpSuccess(false)
                .imapMessage(imapMessage)
                .smtpMessage(smtpMessage)
                .imapResponseTimeMs(0)
                .smtpResponseTimeMs(0)
                .build();
    }
}
