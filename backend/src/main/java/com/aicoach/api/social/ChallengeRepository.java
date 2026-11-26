package com.aicoach.api.social;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {

    Optional<Challenge> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    List<Challenge> findByIdIn(Collection<String> ids);
}
