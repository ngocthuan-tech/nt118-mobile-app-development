package com.aicoach.api.user;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.user.UserDtos.ChangePasswordRequest;
import com.aicoach.api.user.UserDtos.UpdateProfileRequest;
import com.aicoach.api.user.UserDtos.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "2. User Profile", description = "Hồ sơ người dùng đang đăng nhập")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy hồ sơ + tiến độ của tôi")
    public ApiResponse<UserDto> me(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(userService.getMe(userId));
    }

    @PutMapping("/me")
    @Operation(summary = "Sửa hồ sơ (chỉ gửi field cần đổi). Dùng cho khảo sát, sửa hồ sơ, chọn giáo án")
    public ApiResponse<UserDto> update(@AuthenticationPrincipal String userId,
                                       @Valid @RequestBody UpdateProfileRequest body) {
        return ApiResponse.ok("Đã lưu hồ sơ", userService.updateMe(userId, body));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Đổi mật khẩu")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal String userId,
                                            @Valid @RequestBody ChangePasswordRequest body) {
        userService.changePassword(userId, body);
        return ApiResponse.ok("Đã đổi mật khẩu", null);
    }
}
