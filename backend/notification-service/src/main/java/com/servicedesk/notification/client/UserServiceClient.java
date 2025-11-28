package com.servicedesk.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "user-service",
    url = "${servicedesk.services.ticket-service-url:http://localhost:8081}",
    fallback = UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId);

    record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String fullName,
        boolean active
    ) {}
}
