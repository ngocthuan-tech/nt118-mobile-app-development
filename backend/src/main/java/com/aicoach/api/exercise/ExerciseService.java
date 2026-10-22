package com.aicoach.api.exercise;

import com.aicoach.api.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class ExerciseService {

    /** Bài tập trả về cho app — cùng tên field với model Exercise bên Android. */
    public record ExerciseDto(
            String id,
            String name,
            String muscleGroup,
            String difficulty,
            String imageName,
            String description,
            String measureType,
            int defaultSets,
            int defaultReps,
            double caloriesPerRep,
            List<String> steps,
            List<String> tips,
            List<String> commonMistakes) {
    }

    private final ExerciseRepository repo;

    public ExerciseService(ExerciseRepository repo) {
        this.repo = repo;
    }

    /** Lọc theo nhóm cơ, độ khó và từ khoá (không phân biệt dấu: "hit dat" vẫn ra "Hít đất"). */
    @Transactional(readOnly = true)
    public List<ExerciseDto> list(String muscleGroup, String difficulty, String q) {
        String keyword = q == null ? "" : fold(q);
        return repo.findAllByOrderBySortOrderAsc().stream()
                .filter(e -> isBlank(muscleGroup) || muscleGroup.equalsIgnoreCase(e.getMuscleGroup()))
                .filter(e -> isBlank(difficulty) || difficulty.equalsIgnoreCase(e.getDifficulty()))
                .filter(e -> keyword.isEmpty() || fold(e.getName()).contains(keyword) || fold(e.getId()).contains(keyword))
                .map(ExerciseService::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseDto get(String id) {
        return repo.findById(id).map(ExerciseService::toDto)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy bài tập"));
    }

    public static ExerciseDto toDto(Exercise e) {
        return new ExerciseDto(e.getId(), e.getName(), e.getMuscleGroup(), e.getDifficulty(), e.getImageName(),
                e.getDescription(), e.getMeasureType(), e.getDefaultSets(), e.getDefaultReps(),
                e.getCaloriesPerRep(), List.copyOf(e.getSteps()), List.copyOf(e.getTips()),
                List.copyOf(e.getCommonMistakes()));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm. */
    static String fold(String s) {
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccent.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }
}
