package com.servicedesk.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Ticket Service
                .route("ticket-service", r -> r
                        .path("/api/v1/tickets/**", "/api/v1/users/**", "/api/v1/auth/**", "/api/v1/teams/**", "/api/v1/projects/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://ticket-service"))

                // Channel Service
                .route("channel-service", r -> r
                        .path("/api/v1/channels/**", "/api/v1/widget/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://channel-service"))

                // Knowledge Service
                .route("knowledge-service", r -> r
                        .path("/api/v1/knowledge/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://knowledge-service"))

                // AI Service
                .route("ai-service", r -> r
                        .path("/api/v1/ai/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://ai-service"))

                // Notification Service
                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://notification-service"))

                // Analytics Service
                .route("analytics-service", r -> r
                        .path("/api/v1/analytics/**", "/api/v1/reports/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))
                        .uri("lb://analytics-service"))

                .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(100, 200);
    }
}
