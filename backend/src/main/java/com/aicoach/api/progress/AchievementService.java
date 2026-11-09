package com.aicoach.api.progress;

import com.aicoach.api.exercise.ExerciseRepository;
import com.aicoach.api.session.WorkoutSessionRepository;
import com.aicoach.api.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Read-only lifetime XP; never recomputes account XP or grants XP on a screen visit. */
@Service
public class AchievementService {
    public record ExerciseProgress(String exerciseId, String exerciseName, String imageName,
                                   String measureType, long totalXp) { }
    public record Summary(long totalXp, List<ExerciseProgress> exercises) { }

    private final UserService users;
    private final ExerciseRepository exercises;
    private final WorkoutSessionRepository sessions;

    public AchievementService(UserService users, ExerciseRepository exercises, WorkoutSessionRepository sessions) {
        this.users = users;
        this.exercises = exercises;
        this.sessions = sessions;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Summary get(String userId) {
        long totalXp = users.require(userId).getTotalXp();
        Map<String, Long> totals = sessions.sumXpByExercise(userId).stream()
                .collect(Collectors.toMap(WorkoutSessionRepository.ExerciseXp::getExerciseId,
                        WorkoutSessionRepository.ExerciseXp::getXp));
        return new Summary(totalXp, exercises.findAllByOrderBySortOrderAsc().stream()
                .map(e -> new ExerciseProgress(e.getId(), e.getName(), e.getImageName(), e.getMeasureType(),
                        totals.getOrDefault(e.getId(), 0L)))
                .toList());
    }
}
