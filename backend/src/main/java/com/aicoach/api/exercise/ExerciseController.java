package com.aicoach.api.exercise;

import com.aicoach.api.common.ApiResponse;
import com.aicoach.api.exercise.ExerciseService.ExerciseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercises")
@Tag(name = "4. Exercises", description = "Thư viện 9 bài tập")
public class ExerciseController {

    private final ExerciseService service;

    public ExerciseController(ExerciseService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Danh sách bài tập, lọc theo muscleGroup (legs|chest_arms|core|cardio), difficulty (easy|medium|hard), q")
    public ApiResponse<List<ExerciseDto>> list(@RequestParam(required = false) String muscleGroup,
                                               @RequestParam(required = false) String difficulty,
                                               @RequestParam(required = false) String q) {
        return ApiResponse.ok(service.list(muscleGroup, difficulty, q));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết một bài tập (dùng chung layout cho 9 bài)")
    public ApiResponse<ExerciseDto> get(@PathVariable String id) {
        return ApiResponse.ok(service.get(id));
    }
}
