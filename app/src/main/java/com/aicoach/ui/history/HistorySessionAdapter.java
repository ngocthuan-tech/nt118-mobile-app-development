package com.aicoach.ui.history;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aicoach.R;
import com.aicoach.common.ExerciseImages;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.ItemHistorySessionBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

final class HistorySessionAdapter extends RecyclerView.Adapter<HistorySessionAdapter.Holder> {
    private List<WorkoutSession> sessions = new ArrayList<>();
    private final Consumer<WorkoutSession> click;
    HistorySessionAdapter(Consumer<WorkoutSession> click) { this.click = click; }
    void submit(List<WorkoutSession> value) { sessions = new ArrayList<>(value); notifyDataSetChanged(); }
    @Override public int getItemCount() { return sessions.size(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemHistorySessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        WorkoutSession s = sessions.get(position);
        ItemHistorySessionBinding b = holder.binding;
        Context c = b.getRoot().getContext();
        b.tvTitle.setText(c.getString(R.string.progress_session_title, HistoryUi.name(c, s), s.getTotalReps()));
        b.tvSubtitle.setText(c.getString(R.string.progress_session_subtitle, HistoryUi.time(c, s), HistoryUi.duration(s.getDurationSec())));
        int score = HistoryStats.score(s);
        b.tvScore.setText(String.valueOf(score));
        int foreground = score >= 85 ? R.color.state_success : score >= 70 ? R.color.state_warning : R.color.state_error;
        int background = score >= 85 ? R.color.state_success_light : score >= 70 ? R.color.state_warning_light : R.color.state_error_light;
        b.tvScore.setTextColor(ContextCompat.getColor(c, foreground));
        b.tvScore.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(c, background)));
        ExerciseImages.bind(b.imgExercise, s.getExerciseId() == null ? "" : s.getExerciseId());
        boolean enabled = s.getId() != null && !s.getId().trim().isEmpty();
        b.getRoot().setEnabled(enabled);
        b.getRoot().setOnClickListener(v -> { if (enabled) click.accept(s); });
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemHistorySessionBinding binding;
        Holder(ItemHistorySessionBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
