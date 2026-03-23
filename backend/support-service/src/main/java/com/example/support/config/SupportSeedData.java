package com.example.support.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.support.entity.SupportMessage;
import com.example.support.repository.SupportMessageRepository;

@Component
public class SupportSeedData implements CommandLineRunner {

    private final SupportMessageRepository repository;

    public SupportSeedData(SupportMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        SupportMessage welcome = new SupportMessage();
        welcome.setCustomerUsername("demo-customer");
        welcome.setSenderRole("OFFICER");
        welcome.setSenderUsername("officer01");
        welcome.setMessageText("Welcome to support chat. How can we help you today?");
        repository.save(welcome);
    }
}
