package com.aicoach.api.exercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, String> {

    List<Exercise> findAllByOrderBySortOrderAsc();
}
