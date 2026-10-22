package com.aicoach.api.session;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.session.SessionService.CreateSessionRequest;
import com.aicoach.api.session.SessionService.SessionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "6. Workout sessions", description = "Lưu và xem lại buổi tập. Lưu xong server tự cập nhật streak, XP")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Lưu buổi tập vừa hoàn thành")
    public ResponseEntity<ApiResponse<SessionDto>> create(@AuthenticationPrincipal String userId,
                                                          @Valid @RequestBody CreateSessionRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đã lưu buổi tập", service.create(userId, body)));
    }

    @GetMapping
    @Operation(summary = "Lịch sử buổi tập, mới nhất trước (tối đa 100)")
    public ApiResponse<List<SessionDto>> list(@AuthenticationPrincipal String userId,
                                              @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(service.list(userId, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết một buổi tập")
    public ApiResponse<SessionDto> get(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return ApiResponse.ok(service.get(userId, id));
    }
}
