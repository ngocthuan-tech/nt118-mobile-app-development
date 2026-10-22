package com.aicoach.api.plan;

import com.aicoach.api.common.ApiException;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.exercise.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanService {

    public record PlanItemDto(String exerciseId, String exerciseName, String imageName, String measureType,
                              int sets, int reps) {
    }

    public record PlanDayDto(int dayIndex, List<PlanItemDto> items) {
    }

    public record PlanDto(String id, String title, String description, String level, int durationDays,
                          int sessionsPerWeek, int minutesPerSession, String coverImageName,
                          int totalSessions, List<PlanDayDto> days) {
    }

    private final PlanRepository plans;
    private final ExerciseRepository exercises;

    public PlanService(PlanRepository plans, ExerciseRepository exercises) {
        this.plans = plans;
        this.exercises = exercises;
    }

    @Transactional(readOnly = true)
    public List<PlanDto> list() {
        Map<String, Exercise> byId = exerciseMap();
        return plans.findAllByOrderBySortOrderAsc().stream().map(p -> toDto(p, byId)).toList();
    }

    @Transactional(readOnly = true)
    public PlanDto get(String id) {
        Plan p = plans.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy giáo án"));
        return toDto(p, exerciseMap());
    }

    public Map<String, Exercise> exerciseMap() {
        return exercises.findAll().stream().collect(Collectors.toMap(Exercise::getId, Function.identity()));
    }

    public static List<PlanDayDto> days(Plan plan, Map<String, Exercise> byId) {
        Map<Integer, List<PlanItemDto>> grouped = new LinkedHashMap<>();
        for (PlanItem item : plan.getItems()) {
            Exercise ex = byId.get(item.getExerciseId());
            grouped.computeIfAbsent(item.getDayIndex(), k -> new ArrayList<>()).add(new PlanItemDto(
                    item.getExerciseId(),
                    ex != null ? ex.getName() : item.getExerciseId(),
                    ex != null ? ex.getImageName() : null,
                    ex != null ? ex.getMeasureType() : "reps",
                    item.getSets(),
                    item.getReps()));
        }
        return grouped.entrySet().stream().map(e -> new PlanDayDto(e.getKey(), e.getValue())).toList();
    }

    public static PlanDto toDto(Plan p, Map<String, Exercise> byId) {
        List<PlanDayDto> days = days(p, byId);
        return new PlanDto(p.getId(), p.getTitle(), p.getDescription(), p.getLevel(), p.getDurationDays(),
                p.getSessionsPerWeek(), p.getMinutesPerSession(), p.getCoverImageName(), days.size(), days);
    }
}
