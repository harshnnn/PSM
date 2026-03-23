package com.example.support.service;

import java.util.List;

import com.example.support.dto.SendSupportMessageRequest;
import com.example.support.dto.SupportConversationSummaryResponse;
import com.example.support.dto.SupportMessageResponse;
import com.example.support.dto.SupportReadReceiptResponse;

public interface SupportChatService {

    SupportMessageResponse sendMessage(String authenticatedUsername, String authenticatedRole, SendSupportMessageRequest request);

    List<SupportMessageResponse> getConversationMessages(String customerUsername, String authenticatedUsername, String authenticatedRole);

    SupportReadReceiptResponse markConversationRead(String customerUsername, String authenticatedUsername, String authenticatedRole);

    List<SupportConversationSummaryResponse> getOfficerConversations(String authenticatedRole);
}
