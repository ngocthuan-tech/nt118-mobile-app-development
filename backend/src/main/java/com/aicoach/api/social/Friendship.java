package com.aicoach.api.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Một chiều của quan hệ bạn bè (luôn lưu cả 2 chiều A→B và B→A). */
@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "friend_id", nullable = false, length = 36)
    private String friendId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Friendship() {
    }

    public Friendship(String userId, String friendId) {
        this.userId = userId;
        this.friendId = friendId;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getFriendId() { return friendId; }
    public Instant getCreatedAt() { return createdAt; }
}
