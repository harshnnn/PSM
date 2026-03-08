package com.example.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.auth.entity.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByCustomerUsername(String customerUsername);
    Optional<UserAccount> findByCustomerUsername(String customerUsername);
}
