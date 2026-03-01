package com.example.auth.repository;

import com.example.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUserId(String userId);
    boolean existsByUserId(String userId);
    boolean existsByCustomerUsername(String customerUsername);
}
