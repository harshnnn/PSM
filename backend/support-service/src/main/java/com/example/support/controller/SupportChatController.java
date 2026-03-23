package com.example.support.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.support.dto.SendSupportMessageRequest;
import com.example.support.dto.SupportConversationSummaryResponse;
import com.example.support.dto.SupportMessageResponse;
import com.example.support.dto.SupportReadReceiptResponse;
import com.example.support.service.SupportChatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support")
public class SupportChatController {

    private final SupportChatService supportChatService;

    public SupportChatController(SupportChatService supportChatService) {
        this.supportChatService = supportChatService;
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<SupportConversationSummaryResponse>> listConversations(
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        return ResponseEntity.ok(supportChatService.getOfficerConversations(authenticatedRole));
    }

    @GetMapping("/conversations/{customerUsername}/messages")
    public ResponseEntity<List<SupportMessageResponse>> listMessages(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        return ResponseEntity.ok(
                supportChatService.getConversationMessages(customerUsername, authenticatedUsername, authenticatedRole)
        );
    }

    @PostMapping("/messages")
    public ResponseEntity<SupportMessageResponse> sendMessage(
            @RequestBody @Valid SendSupportMessageRequest request,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        return ResponseEntity.ok(
                supportChatService.sendMessage(authenticatedUsername, authenticatedRole, request)
        );
    }

    @PostMapping("/conversations/{customerUsername}/read")
    public ResponseEntity<SupportReadReceiptResponse> markConversationRead(
            @PathVariable String customerUsername,
            @RequestHeader(value = "X-Username", required = false) String authenticatedUsername,
            @RequestHeader(value = "X-User-Role", required = false) String authenticatedRole) {
        return ResponseEntity.ok(
                supportChatService.markConversationRead(customerUsername, authenticatedUsername, authenticatedRole)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage() == null ? "Invalid request" : fieldError.getDefaultMessage())
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
