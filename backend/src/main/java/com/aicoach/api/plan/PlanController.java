package com.aicoach.api.plan;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.plan.PlanService.PlanDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = "5. Plans", description = "Giáo án tập luyện. Chọn giáo án: PUT /api/v1/users/me với activePlanId")
public class PlanController {

    private final PlanService service;

    public PlanController(PlanService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Danh sách giáo án")
    public ApiResponse<List<PlanDto>> list() {
        return ApiResponse.ok(service.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết giáo án: các buổi và bài trong từng buổi")
    public ApiResponse<PlanDto> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }
}
