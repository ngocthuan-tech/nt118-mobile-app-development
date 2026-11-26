package com.aicoach.api.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeInviteRepository extends JpaRepository<ChallengeInvite, String> {

    Optional<ChallengeInvite> findByChallengeIdAndInviteeId(String challengeId, String inviteeId);

    List<ChallengeInvite> findByChallengeId(String challengeId);
}
