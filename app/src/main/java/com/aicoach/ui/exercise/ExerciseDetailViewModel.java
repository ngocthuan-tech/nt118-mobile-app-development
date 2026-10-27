package com.aicoach.ui.exercise;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.aicoach.common.Resource;
import com.aicoach.data.model.Exercise;
import com.aicoach.data.repository.ExerciseRepository;

/** Figma 25 — GET exercises/{id}; tải 1 lần, xoay màn hình không gọi lại. */
public class ExerciseDetailViewModel extends AndroidViewModel {

    private final ExerciseRepository repo;
    private final MediatorLiveData<Resource<Exercise>> exercise = new MediatorLiveData<>();
    private String loadedId;

    public ExerciseDetailViewModel(@NonNull Application app) {
        super(app);
        repo = new ExerciseRepository(app);
    }

    public LiveData<Resource<Exercise>> getExercise() { return exercise; }

    public void load(@NonNull String id) {
        Resource<Exercise> r = exercise.getValue();
        if (id.equals(loadedId) && r != null && r.status != Resource.Status.ERROR) return;
        loadedId = id;
        LiveData<Resource<Exercise>> src = repo.getExercise(id);
        exercise.addSource(src, res -> {
            exercise.setValue(res);
            if (res.status != Resource.Status.LOADING) exercise.removeSource(src);
        });
    }
}
