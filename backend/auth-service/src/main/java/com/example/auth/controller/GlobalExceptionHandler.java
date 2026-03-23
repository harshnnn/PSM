package com.example.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import com.example.auth.exception.AccountLockedException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            if (!errors.containsKey(fieldError.getField())) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Map<String, String>> handleAccountLocked(AccountLockedException ex) {
        Map<String, String> error = new LinkedHashMap<>();
        error.put("code", "ACCOUNT_LOCKED");
        error.put("error", ex.getMessage());
        error.put("supportEmail", ex.getSupportEmail());
        return ResponseEntity.status(HttpStatus.LOCKED).body(error);
    }
}
