package com.servicedesk.notification.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserResponse getUserById(String userId) {
        log.warn("Fallback: Unable to fetch user {} from ticket-service", userId);
        return null;
    }
}
