package com.aicoach.data.model;

import java.util.Collections;
import java.util.List;

/** Dữ liệu Cộng đồng trả về từ server: người có thể mời, bạn bè, bảng xếp hạng, thông báo. */
public final class Social {

    private Social() {
    }

    /** GET /challenges/{id}/candidates — status: NONE (mời được) | INVITED (đang chờ) | MEMBER (đã tham gia). */
    public static final class Candidate {
        public static final String NONE = "NONE";
        public static final String INVITED = "INVITED";
        public static final String MEMBER = "MEMBER";

        private String userId;
        private String displayName;
        private int streak;
        private boolean friend;
        private String status;

        public Candidate() {
        }

        public Candidate(String userId, String displayName, int streak, boolean friend, String status) {
            this.userId = userId;
            this.displayName = displayName;
            this.streak = streak;
            this.friend = friend;
            this.status = status;
        }

        public String getUserId() { return userId; }
        public String getDisplayName() { return displayName == null ? "" : displayName; }
        public int getStreak() { return streak; }
        public boolean isFriend() { return friend; }
        public String getStatus() { return status == null ? NONE : status; }
        public boolean canInvite() { return NONE.equals(getStatus()); }
    }

    /** GET /social/friends */
    public static final class Friend {
        private String userId;
        private String displayName;
        private int streak;
        private long totalXp;

        public String getUserId() { return userId; }
        public String getDisplayName() { return displayName == null ? "" : displayName; }
        public int getStreak() { return streak; }
        public long getTotalXp() { return totalXp; }
    }

    public static final class LeaderboardEntry {
        private int rank;
        private String userId;
        private String displayName;
        private long points;
        private int streak;
        private boolean me;

        public LeaderboardEntry() {
        }

        public LeaderboardEntry(int rank, String userId, String displayName, long points, int streak, boolean me) {
            this.rank = rank;
            this.userId = userId;
            this.displayName = displayName;
            this.points = points;
            this.streak = streak;
            this.me = me;
        }

        public int getRank() { return rank; }
        public String getUserId() { return userId; }
        public String getDisplayName() { return displayName == null ? "" : displayName; }
        public long getPoints() { return points; }
        public int getStreak() { return streak; }
        public boolean isMe() { return me; }
    }

    /** GET /social/leaderboard?scope=friends|national — điểm = XP tích luỹ; me luôn có kể cả khi ngoài top. */
    public static final class Leaderboard {
        private String scope;
        private List<LeaderboardEntry> items;
        private LeaderboardEntry me;

        public Leaderboard() {
        }

        public Leaderboard(String scope, List<LeaderboardEntry> items, LeaderboardEntry me) {
            this.scope = scope;
            this.items = items;
            this.me = me;
        }

        public String getScope() { return scope; }
        public List<LeaderboardEntry> getItems() {
            return items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        }
        public LeaderboardEntry getMe() { return me; }
    }

    /** Một thông báo. Lời mời thử thách có inviteStatus PENDING | ACCEPTED | DECLINED và challengeId. */
    public static final class Notice {
        public static final String CHALLENGE_INVITE = "CHALLENGE_INVITE";
        public static final String PENDING = "PENDING";
        public static final String ACCEPTED = "ACCEPTED";
        public static final String DECLINED = "DECLINED";

        private String id;
        private String type;
        private String title;
        private String body;
        private boolean read;
        private String createdAt;       // ISO, ví dụ "2026-11-26T03:15:00Z"
        private String inviteStatus;
        private String challengeId;

        public Notice() {
        }

        public Notice(String id, String type, String title, String body, boolean read, String createdAt,
                      String inviteStatus, String challengeId) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.body = body;
            this.read = read;
            this.createdAt = createdAt;
            this.inviteStatus = inviteStatus;
            this.challengeId = challengeId;
        }

        public String getId() { return id; }
        public String getType() { return type == null ? "" : type; }
        public String getTitle() { return title == null ? "" : title; }
        public String getBody() { return body == null ? "" : body; }
        public boolean isRead() { return read; }
        public String getCreatedAt() { return createdAt; }
        public String getInviteStatus() { return inviteStatus; }
        public String getChallengeId() { return challengeId; }
        public boolean isChallenge() { return getType().startsWith("CHALLENGE"); }
        public boolean isInvite() { return CHALLENGE_INVITE.equals(type); }
        /** Lời mời còn chờ trả lời → hiện nút Tham gia / Từ chối. */
        public boolean isPendingInvite() { return isInvite() && PENDING.equals(inviteStatus); }
    }

    /** GET /notifications */
    public static final class NoticePage {
        private List<Notice> items;
        private long unreadCount;

        public List<Notice> getItems() {
            return items == null ? Collections.emptyList() : Collections.unmodifiableList(items);
        }
        public long getUnreadCount() { return unreadCount; }
    }

    /** POST /challenges/{id}/invites */
    public static final class InviteResult {
        private int invited;
        private int skipped;

        public int getInvited() { return invited; }
        public int getSkipped() { return skipped; }
    }

    /** POST /notifications/read-all */
    public static final class ReadAllResult {
        private int updated;

        public int getUpdated() { return updated; }
    }
}
