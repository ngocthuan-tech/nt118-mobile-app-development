package com.aicoach.api.social;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.social.SocialDtos.FriendDto;
import com.aicoach.api.social.SocialDtos.LeaderboardDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/social")
@Tag(name = "8. Friends & Leaderboard", description = "Bạn bè và bảng xếp hạng theo XP tích luỹ")
public class SocialController {

    private final FriendService friends;

    public SocialController(FriendService friends) {
        this.friends = friends;
    }

    @GetMapping("/friends")
    @Operation(summary = "Danh sách bạn bè (kết bạn khi cùng tham gia thử thách)")
    public ApiResponse<List<FriendDto>> friends(@AuthenticationPrincipal String userId) {
        return ApiResponse.ok(friends.friends(userId));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Bảng xếp hạng: scope=friends (tôi + bạn bè) hoặc national (top 50)")
    public ApiResponse<LeaderboardDto> leaderboard(@AuthenticationPrincipal String userId,
                                                   @RequestParam(defaultValue = "friends") String scope) {
        return ApiResponse.ok(friends.leaderboard(userId, scope));
    }
}
