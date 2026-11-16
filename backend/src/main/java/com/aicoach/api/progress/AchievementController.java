package com.aicoach.api.progress;

import com.aicoach.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AchievementController {
    private final AchievementService achievements;

    public AchievementController(AchievementService achievements) {
        this.achievements = achievements;
    }

    @GetMapping("/api/v1/users/me/achievements")
    @Operation(summary = "Tổng EXP tài khoản và EXP trọn đời riêng từng bài tập")
    public ApiResponse<AchievementService.Summary> me(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(achievements.get(userId));
    }
}
