package com.aicoach.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aicoach.R;
import com.aicoach.data.model.WorkoutSession;
import com.aicoach.databinding.ItemCalendarDayBinding;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.Holder> {
    private List<LocalDate> days = new ArrayList<>();
    private final Map<LocalDate, Integer> counts = new HashMap<>();
    private LocalDate selected;
    private final Consumer<LocalDate> click;
    CalendarDayAdapter(Consumer<LocalDate> click) { this.click = click; }
    void submit(YearMonth month, LocalDate selected, List<WorkoutSession> sessions) {
        this.selected = selected; days = HistoryStats.calendar(month); counts.clear();
        for (WorkoutSession s : sessions) {
            LocalDate date = HistoryStats.date(s, ZoneId.systemDefault());
            if (date != null) counts.put(date, counts.getOrDefault(date, 0) + 1);
        }
        notifyDataSetChanged();
    }
    @Override public int getItemCount() { return days.size(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        LocalDate day = days.get(position);
        ItemCalendarDayBinding b = holder.binding;
        Context c = b.getRoot().getContext();
        b.getRoot().setVisibility(day == null ? View.INVISIBLE : View.VISIBLE);
        b.getRoot().setOnClickListener(null);
        b.getRoot().setContentDescription(null);
        if (day == null) return;
        int count = counts.getOrDefault(day, 0);
        boolean chosen = day.equals(selected);
        b.tvDay.setText(String.valueOf(day.getDayOfMonth()));
        b.tvDay.setBackgroundResource(chosen ? R.drawable.bg_progress_day_planned
                : count > 0 ? R.drawable.bg_progress_day_done
                : day.equals(LocalDate.now()) ? R.drawable.bg_progress_day_today : 0);
        b.tvDay.setTextColor(ContextCompat.getColor(c, chosen ? R.color.brand_primary : count > 0 ? R.color.white : R.color.text_primary));
        b.getRoot().setSelected(chosen);
        b.getRoot().setContentDescription(c.getString(R.string.progress_day_accessibility,
                day.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), count,
                chosen ? c.getString(R.string.progress_selected) : ""));
        b.getRoot().setOnClickListener(v -> click.accept(day));
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ItemCalendarDayBinding binding;
        Holder(ItemCalendarDayBinding b) { super(b.getRoot()); binding = b; }
    }
}
