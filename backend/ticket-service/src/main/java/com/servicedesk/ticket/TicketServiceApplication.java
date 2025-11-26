package com.servicedesk.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.servicedesk.ticket", "com.servicedesk.common"})
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}
