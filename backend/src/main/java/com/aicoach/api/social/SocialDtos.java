package com.aicoach.api.social;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Dữ liệu vào/ra của module Cộng đồng (thử thách, bạn bè, bảng xếp hạng, thông báo). */
public final class SocialDtos {

    private SocialDtos() {
    }

    // ------------------------------------------------------------------ Thử thách

    public record CreateChallengeRequest(
            @NotBlank(message = "Nhập tên thử thách")
            @Size(min = 3, max = 50, message = "Tên thử thách 3–50 ký tự") String title,
            @NotBlank(message = "Chọn bài tập") String exerciseId,
            @Min(value = 10, message = "Mục tiêu tối thiểu 10 lần mỗi ngày")
            @Max(value = 1000, message = "Mục tiêu tối đa 1000 lần mỗi ngày") int dailyTarget,
            int days,
            boolean privateGroup) {
    }

    /** Tiến độ của 1 thành viên. totalReps = tổng lần đúng trong cả thử thách. */
    public record MemberDto(String userId, String displayName, int todayReps, int totalReps,
                            int completedDays, boolean me) {
    }

    /**
     * status: UPCOMING | ACTIVE | ENDED. daysLeft tính cả hôm nay (ngày cuối = 1).
     * members xếp theo totalReps giảm dần — cũng là bảng xếp hạng trong thử thách.
     */
    public record ChallengeDto(String id, String title, String exerciseId, String exerciseName,
                               String measureType, int dailyTarget, int days, String startDate, String endDate,
                               int daysLeft, String status, boolean privateGroup, String inviteCode,
                               String creatorName, boolean creator, int memberCount,
                               int myTodayReps, int myCompletedDays, List<MemberDto> members) {
    }

    public record InviteRequest(
            @NotEmpty(message = "Chọn ít nhất 1 người")
            @Size(max = 20, message = "Mời tối đa 20 người mỗi lần") List<String> userIds) {
    }

    public record InviteResult(int invited, int skipped) {
    }

    public record JoinRequest(@NotBlank(message = "Nhập mã mời") String code) {
    }

    /** Người có thể mời: status = NONE | INVITED | MEMBER. */
    public record CandidateDto(String userId, String displayName, int streak, boolean friend, String status) {
    }

    // ------------------------------------------------------------------ Bạn bè, bảng xếp hạng

    public record FriendDto(String userId, String displayName, int streak, long totalXp) {
    }

    public record LeaderboardEntry(int rank, String userId, String displayName, long points, int streak, boolean me) {
    }

    /** scope = friends | national. me luôn có (kể cả khi không lọt top). */
    public record LeaderboardDto(String scope, List<LeaderboardEntry> items, LeaderboardEntry me) {
    }

    // ------------------------------------------------------------------ Thông báo

    /**
     * inviteStatus chỉ có với CHALLENGE_INVITE: PENDING | ACCEPTED | DECLINED (app hiện nút Tham gia / Từ chối khi PENDING).
     * challengeId: thử thách liên quan (để app mở đúng thử thách), null với thông báo hệ thống.
     */
    public record NotificationDto(String id, String type, String title, String body, boolean read,
                                  String createdAt, String inviteStatus, String challengeId) {
    }

    public record NotificationList(List<NotificationDto> items, long unreadCount) {
    }

    public record RespondRequest(@NotNull(message = "Chọn Tham gia hoặc Từ chối") Boolean accept) {
    }

    public record ReadAllResult(int updated) {
    }
}
