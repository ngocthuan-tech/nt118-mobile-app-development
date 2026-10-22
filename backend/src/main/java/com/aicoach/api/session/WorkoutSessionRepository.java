package com.aicoach.api.session;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, String> {

    List<WorkoutSession> findByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);

    List<WorkoutSession> findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
            String userId, Instant from, Instant to);

    List<WorkoutSession> findByUserIdAndPlanIdAndStartedAtGreaterThanEqual(String userId, String planId, Instant from);

    Optional<WorkoutSession> findByIdAndUserId(String id, String userId);
}
