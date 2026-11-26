package com.aicoach.api.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Thông báo trong app (Figma 22). */
@Entity
@Table(name = "notifications")
public class Notification {

    /** Có người mời bạn vào thử thách — refId = mã lời mời, có nút Tham gia / Từ chối. */
    public static final String CHALLENGE_INVITE = "CHALLENGE_INVITE";
    /** Có người vừa tham gia thử thách của bạn — refId = mã thử thách. */
    public static final String CHALLENGE_JOINED = "CHALLENGE_JOINED";
    /** Có người từ chối lời mời của bạn — refId = mã thử thách. */
    public static final String CHALLENGE_DECLINED = "CHALLENGE_DECLINED";
    /** Thông báo hệ thống (chào mừng...). */
    public static final String SYSTEM = "SYSTEM";

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(nullable = false, length = 20)
    private String type;
    @Column(nullable = false, length = 120)
    private String title;
    @Column(nullable = false, length = 300)
    private String body;
    @Column(name = "ref_id", length = 36)
    private String refId;
    @Column(name = "is_read", nullable = false)
    private boolean read;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Notification() {
    }

    public Notification(String userId, String type, String title, String body, String refId) {
        this.userId = userId;
        this.type = type;
        this.title = cut(title, 120);
        this.body = cut(body, 300);
        this.refId = refId;
    }

    private static String cut(String s, int max) {
        return s == null ? "" : s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    public boolean isChallenge() {
        return type != null && type.startsWith("CHALLENGE");
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getRefId() { return refId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getCreatedAt() { return createdAt; }
}
