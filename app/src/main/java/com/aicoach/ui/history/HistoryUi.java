package com.aicoach.ui.history;

import android.content.Context;
import android.view.View;
import com.aicoach.R;
import com.aicoach.common.Resource;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.ViewProgressRequestBinding;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class HistoryUi {
    private HistoryUi() {}
    static void request(ViewProgressRequestBinding binding, Resource<?> result) {
        boolean loading = result != null && result.status == Resource.Status.LOADING;
        boolean error = result != null && result.status == Resource.Status.ERROR;
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.error.setVisibility(error ? View.VISIBLE : View.GONE);
        binding.error.setText(error ? result.message : null);
        binding.btnRefresh.setEnabled(!loading);
        binding.btnRefresh.setText(error ? R.string.progress_retry : R.string.progress_refresh);
    }
    static String name(Context context, WorkoutSession session) {
        String name = session.getExerciseName();
        return name == null || name.trim().isEmpty() ? context.getString(R.string.progress_unknown_exercise) : name;
    }
    static String time(Context context, WorkoutSession session) {
        Instant instant = HistoryStats.instant(session);
        return instant == null ? context.getString(R.string.progress_unknown_date)
                : DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm", Locale.getDefault())
                        .format(instant.atZone(HistoryStats.ZONE));
    }
    static String duration(long seconds) {
        seconds = Math.max(0, seconds);
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }
}
