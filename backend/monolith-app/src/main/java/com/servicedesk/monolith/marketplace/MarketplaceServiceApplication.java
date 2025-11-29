package com.servicedesk.monolith.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class MarketplaceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceServiceApplication.class, args);
    }
}
