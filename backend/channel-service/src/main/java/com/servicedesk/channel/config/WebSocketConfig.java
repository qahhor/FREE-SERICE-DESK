package com.servicedesk.channel.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple broker for topics and queues
        config.enableSimpleBroker("/topic", "/queue", "/user");

        // Set application destination prefix
        config.setApplicationDestinationPrefixes("/app");

        // Set user destination prefix for private messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket endpoint for agents
        registry.addEndpoint("/ws/agent")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // WebSocket endpoint for widget/visitors
        registry.addEndpoint("/ws/widget")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // WebSocket endpoint for live chat
        registry.addEndpoint("/ws/livechat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract visitor ID or token from headers for authentication
                    String visitorId = accessor.getFirstNativeHeader("X-Visitor-Id");
                    String token = accessor.getFirstNativeHeader("Authorization");

                    if (visitorId != null) {
                        accessor.setUser(() -> "visitor:" + visitorId);
                        log.debug("WebSocket connected: visitor {}", visitorId);
                    } else if (token != null) {
                        // For authenticated agents, you would validate JWT here
                        log.debug("WebSocket connected: authenticated user");
                    }
                }

                return message;
            }
        });
    }
}
