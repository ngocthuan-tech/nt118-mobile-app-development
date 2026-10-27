package com.aicoach.ui.exercise;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aicoach.common.ExerciseImages;
import com.aicoach.data.model.Exercise;
import com.aicoach.databinding.ItemExerciseGridBinding;

import java.util.Objects;

/** Lưới 3 cột của Thư viện bài tập (Figma 23). */
public class ExerciseGridAdapter extends ListAdapter<Exercise, ExerciseGridAdapter.Holder> {

    public interface OnClick {
        void onExercise(@NonNull Exercise exercise);
    }

    private final OnClick listener;

    public ExerciseGridAdapter(@NonNull OnClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemExerciseGridBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Exercise e = getItem(position);
        h.b.tvName.setText(e.getName());
        h.b.tvDifficulty.setText(ExerciseUi.difficultyLabel(e.getDifficulty()));
        TextViewCompat.setCompoundDrawableTintList(h.b.tvDifficulty, ColorStateList.valueOf(
                ContextCompat.getColor(h.b.getRoot().getContext(), ExerciseUi.difficultyColor(e.getDifficulty()))));
        ExerciseImages.bind(h.b.img, e.getImageName() != null ? e.getImageName() : e.getId());
        h.b.getRoot().setOnClickListener(v -> listener.onExercise(e));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemExerciseGridBinding b;

        Holder(@NonNull ItemExerciseGridBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    private static final DiffUtil.ItemCallback<Exercise> DIFF = new DiffUtil.ItemCallback<Exercise>() {
        @Override
        public boolean areItemsTheSame(@NonNull Exercise a, @NonNull Exercise b) {
            return Objects.equals(a.getId(), b.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Exercise a, @NonNull Exercise b) {
            return Objects.equals(a.getName(), b.getName()) && Objects.equals(a.getDifficulty(), b.getDifficulty())
                    && Objects.equals(a.getImageName(), b.getImageName());
        }
    };
}
