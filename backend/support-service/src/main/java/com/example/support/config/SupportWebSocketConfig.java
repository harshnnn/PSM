package com.example.support.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.support.realtime.SupportRealtimeSessionRegistry;

@Configuration
@EnableWebSocket
public class SupportWebSocketConfig implements WebSocketConfigurer {

    private final SupportRealtimeSessionRegistry realtimeRegistry;

    public SupportWebSocketConfig(SupportRealtimeSessionRegistry realtimeRegistry) {
        this.realtimeRegistry = realtimeRegistry;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(realtimeRegistry, "/ws/support")
                .setAllowedOrigins("*");
    }
}
