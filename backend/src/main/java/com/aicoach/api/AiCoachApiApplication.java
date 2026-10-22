package com.aicoach.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Điểm khởi động REST API của AI Coach.
 * Tắt UserDetailsServiceAutoConfiguration vì hệ thống xác thực bằng JWT, không dùng user mặc định của Spring.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AiCoachApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCoachApiApplication.class, args);
    }
}
