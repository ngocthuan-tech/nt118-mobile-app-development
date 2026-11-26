package com.aicoach.api.social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Lời mời tham gia thử thách: PENDING → ACCEPTED | DECLINED. */
@Entity
@Table(name = "challenge_invites")
public class ChallengeInvite {

    public static final String PENDING = "PENDING";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String DECLINED = "DECLINED";

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();
    @Column(name = "challenge_id", nullable = false, length = 36)
    private String challengeId;
    @Column(name = "inviter_id", nullable = false, length = 36)
    private String inviterId;
    @Column(name = "invitee_id", nullable = false, length = 36)
    private String inviteeId;
    @Column(nullable = false, length = 10)
    private String status = PENDING;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "responded_at")
    private Instant respondedAt;

    protected ChallengeInvite() {
    }

    public ChallengeInvite(String challengeId, String inviterId, String inviteeId) {
        this.challengeId = challengeId;
        this.inviterId = inviterId;
        this.inviteeId = inviteeId;
    }

    /** Mời lại người đã từ chối: mở lại lời mời với người mời mới. */
    public void reopen(String newInviterId) {
        this.inviterId = newInviterId;
        this.status = PENDING;
        this.createdAt = Instant.now();
        this.respondedAt = null;
    }

    public void respond(boolean accept) {
        this.status = accept ? ACCEPTED : DECLINED;
        this.respondedAt = Instant.now();
    }

    public boolean isPending() {
        return PENDING.equals(status);
    }

    public String getId() { return id; }
    public String getChallengeId() { return challengeId; }
    public String getInviterId() { return inviterId; }
    public String getInviteeId() { return inviteeId; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRespondedAt() { return respondedAt; }
}
