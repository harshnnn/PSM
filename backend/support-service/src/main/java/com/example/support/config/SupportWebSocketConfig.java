package com.example.support.config;

import java.util.Objects;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
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
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(Objects.requireNonNull(realtimeRegistry), "/ws/support")
                .setAllowedOrigins("*");
    }
}
