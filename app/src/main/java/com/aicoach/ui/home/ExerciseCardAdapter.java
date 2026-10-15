package com.aicoach.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aicoach.databinding.ItemExerciseCardBinding;

/** Danh sách ngang các thẻ bài tập. Dùng chung cho Trang chủ và (về sau) Thư viện bài tập. */
public class ExerciseCardAdapter extends ListAdapter<ExerciseCardItem, ExerciseCardAdapter.ViewHolder> {

    public interface OnExerciseClick {
        void onClick(@NonNull ExerciseCardItem item);
    }

    private final OnExerciseClick listener;

    public ExerciseCardAdapter(@NonNull OnExerciseClick listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExerciseCardBinding b = ItemExerciseCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExerciseCardItem item = getItem(position);
        holder.binding.tvExerciseName.setText(item.name);
        holder.binding.tvExerciseVolume.setText(item.volume);
        holder.binding.getRoot().setOnClickListener(v -> listener.onClick(item));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemExerciseCardBinding binding;

        ViewHolder(@NonNull ItemExerciseCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<ExerciseCardItem> DIFF =
            new DiffUtil.ItemCallback<ExerciseCardItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ExerciseCardItem a, @NonNull ExerciseCardItem b) {
                    return a.exerciseId.equals(b.exerciseId);
                }

                @Override
                public boolean areContentsTheSame(@NonNull ExerciseCardItem a, @NonNull ExerciseCardItem b) {
                    return a.equals(b);
                }
            };
}
