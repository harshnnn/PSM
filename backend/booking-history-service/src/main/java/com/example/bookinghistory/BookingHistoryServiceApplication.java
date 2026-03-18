package com.example.bookinghistory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
public class BookingHistoryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookingHistoryServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    @SuppressWarnings("unused")
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
