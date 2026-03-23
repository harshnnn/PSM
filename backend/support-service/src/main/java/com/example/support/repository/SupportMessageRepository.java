package com.example.support.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.support.entity.SupportMessage;

public interface SupportMessageRepository extends JpaRepository<SupportMessage, Long> {

    List<SupportMessage> findByCustomerUsernameOrderByCreatedAtAsc(String customerUsername);

    List<SupportMessage> findTop200ByOrderByCreatedAtDesc();

    List<SupportMessage> findByCustomerUsernameAndSenderRoleAndReadAtIsNullOrderByCreatedAtAsc(
            String customerUsername,
            String senderRole
    );

    long countByCustomerUsername(String customerUsername);
}
