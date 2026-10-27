package com.aicoach.api.home;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.home.HomeService.HomeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "3. Home", description = "Toàn bộ dữ liệu Trang chủ trong một lần gọi")
public class HomeController {

    private final HomeService service;

    public HomeController(HomeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Dữ liệu Trang chủ: streak, tiến độ tuần, phút/kcal hôm nay, giáo án, bài tập hôm nay")
    public ApiResponse<HomeDto> home(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(service.getHome(userId));
    }
}
