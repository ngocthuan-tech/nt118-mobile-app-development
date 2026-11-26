package com.aicoach.api.social;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.social.SocialDtos.CandidateDto;
import com.aicoach.api.social.SocialDtos.ChallengeDto;
import com.aicoach.api.social.SocialDtos.CreateChallengeRequest;
import com.aicoach.api.social.SocialDtos.InviteRequest;
import com.aicoach.api.social.SocialDtos.InviteResult;
import com.aicoach.api.social.SocialDtos.JoinRequest;
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
@RequestMapping("/api/v1/challenges")
@Tag(name = "7. Challenges", description = "Thử thách giữa bạn bè: tạo, mời, tham gia bằng mã. Tiến độ tính từ lịch sử buổi tập")
public class ChallengeController {

    private final ChallengeService service;

    public ChallengeController(ChallengeService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Thử thách mình đang tham gia (đang diễn ra trước), kèm tiến độ mọi thành viên")
    public ApiResponse<List<ChallengeDto>> list(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(service.list(userId));
    }

    @PostMapping
    @Operation(summary = "Tạo thử thách (bắt đầu hôm nay, 3/7/14/30 ngày, mục tiêu 10–1000 lần/ngày)")
    public ResponseEntity<ApiResponse<ChallengeDto>> create(@AuthenticationPrincipal String userId,
                                                            @Valid @RequestBody CreateChallengeRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Đã tạo thử thách", service.create(userId, body)));
    }

    @PostMapping("/join")
    @Operation(summary = "Tham gia bằng mã mời 6 ký tự, ví dụ 7Q2KX9")
    public ApiResponse<ChallengeDto> join(@AuthenticationPrincipal String userId,
                                          @Valid @RequestBody JoinRequest body) {
        return ApiResponse.ok("Đã tham gia thử thách", service.joinByCode(userId, body.code()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết một thử thách (chỉ thành viên xem được)")
    public ApiResponse<ChallengeDto> get(@AuthenticationPrincipal String userId, @PathVariable String id) {
        return ApiResponse.ok(service.get(userId, id));
    }

    @GetMapping("/{id}/candidates")
    @Operation(summary = "Người có thể mời: bạn bè (lọc theo tên) hoặc đúng email một người dùng")
    public ApiResponse<List<CandidateDto>> candidates(@AuthenticationPrincipal String userId,
                                                      @PathVariable String id,
                                                      @RequestParam(required = false) String q) {
        return ApiResponse.ok(service.candidates(userId, id, q));
    }

    @PostMapping("/{id}/invites")
    @Operation(summary = "Gửi lời mời (tối đa 20 người mỗi lần) — người được mời nhận thông báo có nút Tham gia")
    public ApiResponse<InviteResult> invite(@AuthenticationPrincipal String userId, @PathVariable String id,
                                            @Valid @RequestBody InviteRequest body) {
        InviteResult result = service.invite(userId, id, body);
        return ApiResponse.ok("Đã gửi " + result.invited() + " lời mời", result);
    }
}
