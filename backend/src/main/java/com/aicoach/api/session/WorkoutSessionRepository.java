package com.aicoach.api.session;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
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

    // ---- Web admin: thống kê toàn hệ thống ----
    long countByUserId(String userId);

    List<WorkoutSession> findByStartedAtGreaterThanEqual(Instant from);

    interface FormErrorCount {
        String getCode();
        long getTotal();
    }

    /** Lỗi tư thế AI bắt được nhiều nhất từ thời điểm from (cộng dồn bảng session_form_errors). */
    @Query(value = "SELECT fe.error_code AS code, CAST(SUM(fe.error_count) AS BIGINT) AS total "
            + "FROM session_form_errors fe JOIN workout_sessions s ON s.id = fe.session_id "
            + "WHERE s.started_at >= :from AND fe.error_count > 0 "
            + "GROUP BY fe.error_code ORDER BY total DESC LIMIT 10", nativeQuery = true)
    List<FormErrorCount> topFormErrors(@Param("from") Instant from);

    interface ExerciseCount {
        String getExerciseId();
        long getSessions();
    }

    @Query("select s.exerciseId as exerciseId, count(s) as sessions from WorkoutSession s group by s.exerciseId")
    List<ExerciseCount> countByExercise();

    /** Tiến độ thử thách: buổi tập của các thành viên, cùng bài, trong khoảng thời gian [from, to). */
    List<WorkoutSession> findByUserIdInAndExerciseIdAndStartedAtGreaterThanEqualAndStartedAtLessThan(
            Collection<String> userIds, String exerciseId, Instant from, Instant to);
}
