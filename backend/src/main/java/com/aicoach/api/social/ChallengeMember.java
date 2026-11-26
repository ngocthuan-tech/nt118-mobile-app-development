package com.aicoach.api.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Một người tham gia thử thách. */
@Entity
@Table(name = "challenge_members")
public class ChallengeMember {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "challenge_id", nullable = false, length = 36)
    private String challengeId;
    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    protected ChallengeMember() {
    }

    public ChallengeMember(String challengeId, String userId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    public String getId() { return id; }
    public String getChallengeId() { return challengeId; }
    public String getUserId() { return userId; }
    public Instant getJoinedAt() { return joinedAt; }
}
