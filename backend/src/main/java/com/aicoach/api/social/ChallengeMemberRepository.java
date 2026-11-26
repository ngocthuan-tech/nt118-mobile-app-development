package com.aicoach.api.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, String> {

    List<ChallengeMember> findByUserId(String userId);

    List<ChallengeMember> findByChallengeIdOrderByJoinedAtAsc(String challengeId);

    boolean existsByChallengeIdAndUserId(String challengeId, String userId);

    long countByChallengeId(String challengeId);
}
