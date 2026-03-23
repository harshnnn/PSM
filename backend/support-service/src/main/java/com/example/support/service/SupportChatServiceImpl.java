package com.example.support.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.support.dto.SendSupportMessageRequest;
import com.example.support.dto.SupportConversationSummaryResponse;
import com.example.support.dto.SupportMessageResponse;
import com.example.support.dto.SupportReadReceiptResponse;
import com.example.support.entity.SupportMessage;
import com.example.support.realtime.SupportRealtimeSessionRegistry;
import com.example.support.repository.SupportMessageRepository;

@Service
@Transactional
public class SupportChatServiceImpl implements SupportChatService {

    private static final String ROLE_CUSTOMER = "CUSTOMER";
    private static final String ROLE_OFFICER = "OFFICER";

    private final SupportMessageRepository messageRepository;
    private final SupportRealtimeSessionRegistry realtimeRegistry;

    public SupportChatServiceImpl(
            SupportMessageRepository messageRepository,
            SupportRealtimeSessionRegistry realtimeRegistry
    ) {
        this.messageRepository = messageRepository;
        this.realtimeRegistry = realtimeRegistry;
    }

    @Override
    public SupportMessageResponse sendMessage(String authenticatedUsername, String authenticatedRole, SendSupportMessageRequest request) {
        String normalizedRole = normalizeRole(authenticatedRole);
        String normalizedAuthUsername = normalizeUsername(authenticatedUsername);
        String targetCustomerUsername = switch (normalizedRole) {
            case ROLE_CUSTOMER -> normalizedAuthUsername;
            case ROLE_OFFICER -> normalizeUsername(request.getCustomerUsername());
            default -> throw new IllegalArgumentException("Unsupported role");
        };

        if (targetCustomerUsername.isBlank()) {
            throw new IllegalArgumentException("Customer username is required");
        }

        String messageText = request.getMessage() == null ? "" : request.getMessage().trim();
        if (messageText.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }

        SupportMessage message = new SupportMessage();
        message.setCustomerUsername(targetCustomerUsername);
        message.setSenderRole(normalizedRole);
        message.setSenderUsername(normalizedAuthUsername);
        message.setMessageText(messageText);

        SupportMessage saved = messageRepository.save(message);

        if (shouldMarkDelivered(saved)) {
            saved.setDeliveredAt(LocalDateTime.now());
            saved = messageRepository.save(saved);
        }

        SupportMessageResponse response = toResponse(saved);
        realtimeRegistry.publishMessage(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportMessageResponse> getConversationMessages(String customerUsername, String authenticatedUsername, String authenticatedRole) {
        String normalizedRole = normalizeRole(authenticatedRole);
        String normalizedAuthUsername = normalizeUsername(authenticatedUsername);
        String targetCustomer = normalizeUsername(customerUsername);

        if (targetCustomer.isBlank()) {
            throw new IllegalArgumentException("Customer username is required");
        }

        if (ROLE_CUSTOMER.equals(normalizedRole) && !targetCustomer.equalsIgnoreCase(normalizedAuthUsername)) {
            throw new IllegalArgumentException("Customers can only access their own support conversation");
        }

        if (!ROLE_CUSTOMER.equals(normalizedRole) && !ROLE_OFFICER.equals(normalizedRole)) {
            throw new IllegalArgumentException("Unsupported role");
        }

        return messageRepository.findByCustomerUsernameOrderByCreatedAtAsc(targetCustomer)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SupportReadReceiptResponse markConversationRead(
            String customerUsername,
            String authenticatedUsername,
            String authenticatedRole
    ) {
        String normalizedRole = normalizeRole(authenticatedRole);
        String normalizedAuthUsername = normalizeUsername(authenticatedUsername);
        String targetCustomer = normalizeUsername(customerUsername);

        if (targetCustomer.isBlank()) {
            throw new IllegalArgumentException("Customer username is required");
        }

        if (ROLE_CUSTOMER.equals(normalizedRole) && !targetCustomer.equalsIgnoreCase(normalizedAuthUsername)) {
            throw new IllegalArgumentException("Customers can only mark their own support conversation");
        }

        if (!ROLE_CUSTOMER.equals(normalizedRole) && !ROLE_OFFICER.equals(normalizedRole)) {
            throw new IllegalArgumentException("Unsupported role");
        }

        String incomingSenderRole = ROLE_CUSTOMER.equals(normalizedRole) ? ROLE_OFFICER : ROLE_CUSTOMER;
        List<SupportMessage> unread = messageRepository
                .findByCustomerUsernameAndSenderRoleAndReadAtIsNullOrderByCreatedAtAsc(targetCustomer, incomingSenderRole);

        LocalDateTime readAt = LocalDateTime.now();
        List<Long> messageIds = new ArrayList<>();

        for (SupportMessage message : unread) {
            message.setReadAt(readAt);
            message.setReadByUsername(normalizedAuthUsername);
            if (message.getDeliveredAt() == null) {
                message.setDeliveredAt(readAt);
            }
            messageIds.add(message.getId());
        }

        if (!unread.isEmpty()) {
            messageRepository.saveAll(unread);
        }

        SupportReadReceiptResponse response = new SupportReadReceiptResponse();
        response.setCustomerUsername(targetCustomer);
        response.setReadByUsername(normalizedAuthUsername);
        response.setReadByRole(normalizedRole);
        response.setReadAt(readAt);
        response.setMessageIds(messageIds);

        realtimeRegistry.publishReadReceipt(response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportConversationSummaryResponse> getOfficerConversations(String authenticatedRole) {
        if (!ROLE_OFFICER.equals(normalizeRole(authenticatedRole))) {
            throw new IllegalArgumentException("Only officers can list all conversations");
        }

        List<SupportMessage> recentMessages = messageRepository.findTop200ByOrderByCreatedAtDesc();
        Map<String, SupportConversationSummaryResponse> summaries = new LinkedHashMap<>();

        for (SupportMessage message : recentMessages) {
            String customerUsername = message.getCustomerUsername();
            if (!summaries.containsKey(customerUsername)) {
                SupportConversationSummaryResponse summary = new SupportConversationSummaryResponse();
                summary.setCustomerUsername(customerUsername);
                summary.setLastMessage(message.getMessageText());
                summary.setLastSenderRole(message.getSenderRole());
                summary.setLastUpdatedAt(message.getCreatedAt());
                summary.setMessageCount(messageRepository.countByCustomerUsername(customerUsername));
                summaries.put(customerUsername, summary);
            }
        }

        return new ArrayList<>(summaries.values());
    }

    private SupportMessageResponse toResponse(SupportMessage message) {
        SupportMessageResponse response = new SupportMessageResponse();
        response.setId(message.getId());
        response.setCustomerUsername(message.getCustomerUsername());
        response.setSenderRole(message.getSenderRole());
        response.setSenderUsername(message.getSenderUsername());
        response.setMessage(message.getMessageText());
        response.setCreatedAt(message.getCreatedAt());
        response.setDeliveredAt(message.getDeliveredAt());
        response.setReadAt(message.getReadAt());
        response.setReadByUsername(message.getReadByUsername());
        return response;
    }

    private boolean shouldMarkDelivered(SupportMessage message) {
        if (ROLE_CUSTOMER.equals(message.getSenderRole())) {
            return realtimeRegistry.isOfficerOnline();
        }
        return realtimeRegistry.isCustomerOnline(message.getCustomerUsername());
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }
}
