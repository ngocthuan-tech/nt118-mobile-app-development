package com.aicoach.api.health;

import com.aicoach.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;

@RestController
@Tag(name = "0. Health", description = "Kiểm tra server còn sống")
@SecurityRequirements
public class HealthController {

    public record Health(String status, String database, Instant time) {
    }

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public HealthController(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @GetMapping("/api/v1/health")
    @Operation(summary = "Server và database có đang chạy không")
    public ApiResponse<Health> health() {
        String db;
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            db = "UP";
        } catch (Exception e) {
            db = "DOWN";
        }
        return ApiResponse.ok(new Health("UP", db, clock.instant()));
    }
}
