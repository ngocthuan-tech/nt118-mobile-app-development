package com.aicoach.ui.history;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.aicoach.R;
import com.aicoach.data.model.WorkoutSession;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

final class HistoryChart {
    private HistoryChart() {}
    static void render(BarChart chart, List<WorkoutSession> sessions, int range, LocalDate today, String title) {
        LocalDate start = HistoryStats.rangeStart(range, today);
        int size = range == 2 ? 12 : range == 1 ? today.lengthOfMonth() : 7;
        float[] values = new float[size];
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < size; i++) labels.add(range == 2 ? String.valueOf(i + 1)
                : start.plusDays(i).format(DateTimeFormatter.ofPattern("dd/MM")));
        for (WorkoutSession session : sessions) {
            LocalDate day = HistoryStats.date(session, HistoryStats.ZONE);
            if (day == null) continue;
            int index = range == 2 ? day.getMonthValue() - 1 : (int) ChronoUnit.DAYS.between(start, day);
            if (index >= 0 && index < size) values[index] += Math.max(0, session.getTotalReps());
        }
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) entries.add(new BarEntry(i, values[i]));
        Context c = chart.getContext();
        BarDataSet set = new BarDataSet(entries, title);
        set.setColor(ContextCompat.getColor(c, R.color.brand_primary));
        set.setDrawValues(false);
        chart.setData(new BarData(set));
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisLeft().setGranularity(1);
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setGranularity(1);
        chart.getXAxis().setLabelCount(Math.min(7, size));
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(ContextCompat.getColor(c, R.color.text_secondary));
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chart.setScaleEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setContentDescription(c.getString(R.string.progress_chart_accessibility,
                title, HistoryStats.reps(sessions), sessions.size()));
        chart.invalidate();
    }
}
