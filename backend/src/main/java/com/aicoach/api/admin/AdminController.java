package com.aicoach.api.admin;

import com.aicoach.api.admin.AdminDtos.AdminMe;
import com.aicoach.api.admin.AdminDtos.ExerciseRow;
import com.aicoach.api.admin.AdminDtos.ExerciseUpdate;
import com.aicoach.api.admin.AdminDtos.LockRequest;
import com.aicoach.api.admin.AdminDtos.PageResult;
import com.aicoach.api.admin.AdminDtos.PlanItemUpdate;
import com.aicoach.api.admin.AdminDtos.PlanRow;
import com.aicoach.api.admin.AdminDtos.Stats;
import com.aicoach.api.admin.AdminDtos.UserDetail;
import com.aicoach.api.admin.AdminDtos.UserRow;
import com.aicoach.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Web quản trị. Toàn bộ /api/v1/admin/** chỉ tài khoản ADMIN gọi được (chặn trong SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "10. Admin", description = "Web quản trị: thống kê, người dùng (khoá / mở khoá), bài tập, giáo án — chỉ ADMIN")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @Operation(summary = "Tài khoản quản trị đang đăng nhập (web dùng để xác nhận quyền ADMIN)")
    public ApiResponse<AdminMe> me(@AuthenticationPrincipal String adminId) {
        return ApiResponse.ok(service.me(adminId));
    }

    @GetMapping("/stats")
    @Operation(summary = "Tổng quan: người dùng, buổi tập theo ngày, bài phổ biến, lỗi tư thế AI hay gặp (days 7–90)")
    public ApiResponse<Stats> stats(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(service.stats(days));
    }

    @GetMapping("/users")
    @Operation(summary = "Danh sách người dùng (mới nhất trước), tìm theo email / tên không dấu, phân trang")
    public ApiResponse<PageResult<UserRow>> users(@RequestParam(required = false) String q,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.users(q, page, size));
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Chi tiết người dùng + 10 buổi tập gần nhất")
    public ApiResponse<UserDetail> user(@PathVariable String id) {
        return ApiResponse.ok(service.user(id));
    }

    @PutMapping("/users/{id}/lock")
    @Operation(summary = "Khoá / mở khoá tài khoản. Khoá thì token đang dùng mất hiệu lực ngay; không tự khoá mình")
    public ApiResponse<UserRow> lock(@AuthenticationPrincipal String adminId, @PathVariable String id,
                                     @Valid @RequestBody LockRequest body) {
        UserRow row = service.setLocked(adminId, id, body.locked());
        return ApiResponse.ok(row.locked() ? "Đã khoá tài khoản" : "Đã mở khoá tài khoản", row);
    }

    @GetMapping("/exercises")
    @Operation(summary = "9 bài tập kèm số buổi đã tập")
    public ApiResponse<List<ExerciseRow>> exercises() {
        return ApiResponse.ok(service.exercises());
    }

    @PutMapping("/exercises/{id}")
    @Operation(summary = "Sửa tên, mô tả, số hiệp, số lần, kcal — app thấy ngay qua GET /exercises")
    public ApiResponse<ExerciseRow> updateExercise(@PathVariable String id, @Valid @RequestBody ExerciseUpdate body) {
        return ApiResponse.ok("Đã lưu bài tập", service.updateExercise(id, body));
    }

    @GetMapping("/plans")
    @Operation(summary = "Giáo án kèm từng buổi và bài, số người đang theo")
    public ApiResponse<List<PlanRow>> plans() {
        return ApiResponse.ok(service.plans());
    }

    @PutMapping("/plans/{planId}/items/{itemId}")
    @Operation(summary = "Sửa số hiệp × lần của một bài trong giáo án — app dùng ngay số mới")
    public ApiResponse<PlanRow> updatePlanItem(@PathVariable String planId, @PathVariable long itemId,
                                               @Valid @RequestBody PlanItemUpdate body) {
        return ApiResponse.ok("Đã lưu giáo án", service.updatePlanItem(planId, itemId, body));
    }
}
