package com.aicoach.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Cấu hình chung: đồng hồ hệ thống (tách ra để test được) và tài liệu Swagger có nút "Authorize" nhập JWT.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "AI Coach API",
                version = "v1",
                description = "REST API cho app AI Coach — NT118 Nhóm 8. "
                        + "Bấm Authorize, dán accessToken lấy từ /api/v1/auth/login để gọi các API cần đăng nhập."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
