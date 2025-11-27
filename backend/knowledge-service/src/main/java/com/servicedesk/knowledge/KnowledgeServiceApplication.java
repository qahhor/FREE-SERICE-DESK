package com.servicedesk.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.servicedesk.knowledge", "com.servicedesk.common"})
public class KnowledgeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeServiceApplication.class, args);
    }
}
