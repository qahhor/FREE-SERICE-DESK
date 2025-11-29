package com.servicedesk.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ServiceDesk Monolith Application
 *
 * This is a unified monolithic application that combines all microservices:
 * - Ticket Service (Core ITSM functionality)
 * - Channel Service (Email, Telegram, WhatsApp, LiveChat, Widget)
 * - Notification Service (In-app, Email, Push notifications)
 * - Knowledge Service (Knowledge base and articles)
 * - AI Service (OpenAI/Claude integration, RAG)
 * - Analytics Service (Dashboards and reports)
 * - Marketplace Service (Module marketplace)
 *
 * Benefits of monolithic architecture:
 * - Simplified deployment (single JAR file)
 * - No network latency between modules
 * - ACID transactions across all modules
 * - Easier development and debugging
 * - Lower infrastructure requirements (no RabbitMQ for inter-service communication)
 *
 * The application uses:
 * - Spring Events instead of RabbitMQ for internal communication
 * - Direct method calls instead of FeignClient
 * - Single PostgreSQL database with table prefixes per module
 * - Redis for caching
 * - Elasticsearch for search and RAG
 */
@SpringBootApplication(scanBasePackages = "com.servicedesk")
@EnableCaching
@EnableAsync
@EnableScheduling
public class ServiceDeskMonolithApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDeskMonolithApplication.class, args);
    }
}
