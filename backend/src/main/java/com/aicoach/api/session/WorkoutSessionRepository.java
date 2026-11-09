package com.aicoach.api.session;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, String> {

    interface ExerciseXp {
        String getExerciseId();
        long getXp();
    }

    /** Aggregate in SQL over the entire history, scoped to the authenticated user.
     * Valid sessions have 0 <= correctReps <= totalReps, so XP = totalReps + correctReps.
     */
    @Query("select s.exerciseId as exerciseId, sum(cast(s.totalReps as long) + s.correctReps) as xp "
            + "from WorkoutSession s where s.userId = :userId group by s.exerciseId")
    List<ExerciseXp> sumXpByExercise(@Param("userId") String userId);

    List<WorkoutSession> findByUserIdOrderByStartedAtDesc(String userId, Pageable pageable);

    List<WorkoutSession> findByUserIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
            String userId, Instant from, Instant to);

    List<WorkoutSession> findByUserIdAndPlanIdAndStartedAtGreaterThanEqual(String userId, String planId, Instant from);

    Optional<WorkoutSession> findByIdAndUserId(String id, String userId);
}
