package com.aicoach.api.social;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.social.SocialDtos.NotificationDto;
import com.aicoach.api.social.SocialDtos.NotificationList;
import com.aicoach.api.social.SocialDtos.ReadAllResult;
import com.aicoach.api.social.SocialDtos.RespondRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "9. Notifications", description = "Thông báo trong app: lời mời thử thách, người tham gia, chào mừng")
public class NotificationController {

    private final NotificationService notifications;
    private final ChallengeService challenges;

    public NotificationController(NotificationService notifications, ChallengeService challenges) {
        this.notifications = notifications;
        this.challenges = challenges;
    }

    @GetMapping
    @Operation(summary = "Thông báo mới nhất trước (tối đa 50): type=all | challenge | system")
    public ApiResponse<NotificationList> list(@AuthenticationPrincipal String userId,
                                              @RequestParam(defaultValue = "all") String type) {
        return ApiResponse.ok(notifications.list(userId, type));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả đã đọc")
    public ApiResponse<ReadAllResult> readAll(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(notifications.readAll(userId));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    public ApiResponse<NotificationDto> read(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return ApiResponse.ok(notifications.read(userId, id));
    }

    @PostMapping("/{id}/respond")
    @Operation(summary = "Trả lời lời mời thử thách: {\"accept\": true} = Tham gia, false = Từ chối")
    public ApiResponse<NotificationDto> respond(@AuthenticationPrincipal String userId, @PathVariable String id,
                                                @Valid @RequestBody RespondRequest body) {
        NotificationDto dto = challenges.respond(userId, id, body.accept());
        return ApiResponse.ok(Boolean.TRUE.equals(body.accept()) ? "Đã tham gia thử thách" : "Đã từ chối lời mời", dto);
    }
}
