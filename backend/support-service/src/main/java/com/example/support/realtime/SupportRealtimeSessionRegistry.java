package com.example.support.realtime;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.support.dto.SupportMessageResponse;
import com.example.support.dto.SupportReadReceiptResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SupportRealtimeSessionRegistry extends TextWebSocketHandler {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_OFFICER = "OFFICER";

    private final Map<String, ConnectedClient> connectedClients = new HashMap<>();
    private final ObjectMapper objectMapper;

    public SupportRealtimeSessionRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized void afterConnectionEstablished(@NonNull WebSocketSession session) {
        Map<String, String> query = parseQuery(session.getUri());
        String username = normalize(query.get("username"));
        String role = normalizeRole(query.get("role"));

        if (username.isBlank() || (!ROLE_CUSTOMER.equals(role) && !ROLE_OFFICER.equals(role))) {
            closeQuietly(session, Objects.requireNonNull(CloseStatus.BAD_DATA));
            return;
        }

        ConnectedClient client = new ConnectedClient();
        client.setSession(session);
        client.setUsername(username);
        client.setRole(role);

        connectedClients.put(session.getId(), client);
    }

    @Override
    public synchronized void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        connectedClients.remove(session.getId());
    }

    @Override
    protected synchronized void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        ConnectedClient client = connectedClients.get(session.getId());
        if (client == null) {
            return;
        }

        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            if (!"presence".equalsIgnoreCase(payload.path("type").asText(""))) {
                return;
            }

            String activeCustomer = normalize(payload.path("activeCustomerUsername").asText(""));
            if (ROLE_CUSTOMER.equals(client.getRole())) {
                // Customer presence does not need per-conversation state.
            } else {
                // Officer may send active customer in presence payload for future use.
                Objects.requireNonNull(activeCustomer);
            }
        } catch (IOException ignored) {
            // Ignore malformed realtime client payloads.
        }
    }

    public synchronized boolean isOfficerOnline() {
        return connectedClients.values().stream().anyMatch(client -> ROLE_OFFICER.equals(client.getRole()));
    }

    public synchronized boolean isCustomerOnline(String customerUsername) {
        String normalized = normalize(customerUsername);
        return connectedClients.values().stream().anyMatch(client ->
                ROLE_CUSTOMER.equals(client.getRole()) && normalized.equalsIgnoreCase(client.getUsername())
        );
    }

    public synchronized void publishMessage(SupportMessageResponse message) {
        sendToParticipants(message.getCustomerUsername(), "message", message);
    }

    public synchronized void publishReadReceipt(SupportReadReceiptResponse receipt) {
        if (receipt.getMessageIds().isEmpty()) {
            return;
        }
        sendToParticipants(receipt.getCustomerUsername(), "read_receipt", receipt);
    }

    private void sendToParticipants(String customerUsername, String type, Object payloadObject) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("payload", payloadObject);

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (IOException e) {
            return;
        }

        for (ConnectedClient client : connectedClients.values()) {
            if (ROLE_OFFICER.equals(client.getRole()) || customerUsername.equalsIgnoreCase(client.getUsername())) {
                sendQuietly(Objects.requireNonNull(client.getSession()), Objects.requireNonNull(json));
            }
        }
    }

    private void sendQuietly(@NonNull WebSocketSession session, @NonNull String payload) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(Objects.requireNonNull(payload)));
            }
        } catch (IOException ignored) {
            // Connection may have dropped between lookup and send.
        }
    }

    private void closeQuietly(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        try {
            session.close(Objects.requireNonNull(status));
        } catch (IOException ignored) {
            // No-op.
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        if (uri == null || uri.getRawQuery() == null || uri.getRawQuery().isBlank()) {
            return result;
        }

        String[] pairs = uri.getRawQuery().split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }

    private String normalizeRole(String role) {
        return normalize(role).toUpperCase();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static class ConnectedClient {
        private WebSocketSession session;
        private String username;
        private String role;

        public WebSocketSession getSession() {
            return session;
        }

        public void setSession(WebSocketSession session) {
            this.session = session;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
