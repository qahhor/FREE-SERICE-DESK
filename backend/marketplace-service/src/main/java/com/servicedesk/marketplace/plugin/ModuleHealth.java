package com.servicedesk.marketplace.plugin;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Health status of a module
 */
@Data
@Builder
public class ModuleHealth {

    private Status status;
    private String message;
    private Map<String, Object> details;

    public enum Status {
        HEALTHY,
        DEGRADED,
        UNHEALTHY
    }

    public static ModuleHealth healthy() {
        return ModuleHealth.builder()
                .status(Status.HEALTHY)
                .message("Module is healthy")
                .build();
    }

    public static ModuleHealth healthy(String message) {
        return ModuleHealth.builder()
                .status(Status.HEALTHY)
                .message(message)
                .build();
    }

    public static ModuleHealth degraded(String message) {
        return ModuleHealth.builder()
                .status(Status.DEGRADED)
                .message(message)
                .build();
    }

    public static ModuleHealth degraded(String message, Map<String, Object> details) {
        return ModuleHealth.builder()
                .status(Status.DEGRADED)
                .message(message)
                .details(details)
                .build();
    }

    public static ModuleHealth unhealthy(String message) {
        return ModuleHealth.builder()
                .status(Status.UNHEALTHY)
                .message(message)
                .build();
    }

    public static ModuleHealth unhealthy(String message, Map<String, Object> details) {
        return ModuleHealth.builder()
                .status(Status.UNHEALTHY)
                .message(message)
                .details(details)
                .build();
    }
}
