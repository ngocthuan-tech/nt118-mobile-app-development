package com.aicoach.api.home;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.home.HomeService.HomeDto;
import com.aicoach.api.home.HomeService.StreakDto;
import com.aicoach.api.home.HomeService.TodayWorkoutDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "3. Home", description = "Trang chủ: Chuỗi 7 ngày, Bài tập hôm nay")
public class HomeController {

    private final HomeService service;

    public HomeController(HomeService service) {
        this.service = service;
    }

    @GetMapping("/streak")
    @Operation(summary = "Chuỗi 7 ngày: số ngày tập liên tiếp + 7 ngày T2–CN tuần này (ngày nào đã tập, hôm nay)")
    public ApiResponse<StreakDto> streak(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(service.getStreak(userId));
    }

    @GetMapping("/today-workout")
    @Operation(summary = "Bài tập hôm nay: buổi tiếp theo của giáo án đang theo, hoặc 3 bài gợi ý nếu chưa chọn giáo án")
    public ApiResponse<TodayWorkoutDto> todayWorkout(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(service.getTodayWorkout(userId));
    }

    @GetMapping
    @Operation(summary = "Gộp toàn bộ Trang chủ trong một lần gọi (streak, tuần, phút/kcal, giáo án, bài hôm nay)")
    public ApiResponse<HomeDto> home(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(service.getHome(userId));
    }
}
