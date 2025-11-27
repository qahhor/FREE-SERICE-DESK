package com.servicedesk.channel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.servicedesk.channel", "com.servicedesk.common"})
@EnableAsync
@EnableScheduling
public class ChannelServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChannelServiceApplication.class, args);
    }
}
