package com.aicoach.ui.plan;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aicoach.R;
import com.aicoach.common.ExerciseImages;
import com.aicoach.data.model.Plan;
import com.aicoach.databinding.ItemPlanExerciseRowBinding;
import com.aicoach.databinding.ItemPlanSessionBinding;
import com.aicoach.ui.exercise.ExerciseUi;

/** Danh sách buổi tập của giáo án — dùng cho 17 (tuần 1, dạng gọn) và 26 (từng tuần, có trạng thái). */
public class PlanSessionAdapter extends ListAdapter<PlanSessionItem, PlanSessionAdapter.Holder> {

    public interface Listener {
        void onStartSession(@NonNull PlanSessionItem session);

        void onExercise(@NonNull Plan.PlanItem item);
    }

    private final Listener listener;

    public PlanSessionAdapter(@NonNull Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemPlanSessionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PlanSessionItem s = getItem(position);
        ItemPlanSessionBinding b = h.binding;
        Context c = b.getRoot().getContext();

        b.tvDay.setText(s.dayLabel);
        b.tvSessionNo.setText(c.getString(R.string.plan_session_number, s.sessionNumber));
        // 26 đã ghi "Buổi 5" ở tiêu đề → ô ngày chỉ còn "T4" như Figma
        b.tvSessionNo.setVisibility(s.compact ? View.VISIBLE : View.GONE);

        boolean today = s.state == PlanSessionItem.State.TODAY;
        boolean filledDay = s.highlighted || today;
        if (filledDay) {
            b.dayBox.setBackgroundResource(R.drawable.bg_auth_gradient_square);
            b.dayBox.setBackgroundTintList(null);
        } else {
            b.dayBox.setBackgroundResource(R.drawable.bg_icon_box);
            b.dayBox.setBackgroundTintList(ColorStateList.valueOf(color(c,
                    s.state == PlanSessionItem.State.DONE ? R.color.state_success_light : R.color.brand_primary_light)));
        }
        @ColorRes int dayColor = filledDay ? R.color.white
                : s.state == PlanSessionItem.State.DONE ? R.color.state_success
                : s.state == PlanSessionItem.State.LOCKED ? R.color.text_secondary : R.color.brand_primary;
        b.tvDay.setTextColor(color(c, dayColor));
        b.tvSessionNo.setTextColor(color(c, filledDay ? R.color.white : R.color.text_secondary));

        if (s.compact) {
            b.tvTitle.setText(s.exerciseNames);
            b.tvSubtitle.setText(c.getString(R.string.plan_minutes, s.minutes));
        } else if (today) {
            b.tvTitle.setText(c.getString(R.string.plan_session_today, s.sessionNumber));
            b.tvSubtitle.setText(c.getString(R.string.plan_session_summary, s.items.size(), s.minutes));
        } else {
            b.tvTitle.setText(c.getString(R.string.plan_session_number, s.sessionNumber));
            b.tvSubtitle.setText(c.getString(R.string.plan_session_line, s.exerciseNames, s.minutes));
        }
        b.tvTitle.setTextColor(color(c, s.state == PlanSessionItem.State.LOCKED
                ? R.color.text_secondary : R.color.text_primary));

        b.card.setStrokeWidth(today || s.highlighted
                ? c.getResources().getDimensionPixelSize(R.dimen.space_2) : 0);

        // Bên phải: ảnh nhỏ (17) | nút Bắt đầu (hôm nay) | tích / khoá
        b.rowThumbs.setVisibility(s.compact ? View.VISIBLE : View.GONE);
        if (s.compact) {
            ImageView[] thumbs = {b.thumb1, b.thumb2, b.thumb3};
            for (int i = 0; i < thumbs.length; i++) {
                boolean has = i < s.items.size();
                thumbs[i].setVisibility(has ? View.VISIBLE : View.GONE);
                if (has) bindImage(thumbs[i], s.items.get(i));
            }
        }
        b.btnStart.setVisibility(!s.compact && today ? View.VISIBLE : View.GONE);
        b.btnStart.setOnClickListener(v -> listener.onStartSession(s));
        boolean showStatus = !s.compact && (s.state == PlanSessionItem.State.DONE
                || s.state == PlanSessionItem.State.LOCKED);
        b.imgStatus.setVisibility(showStatus ? View.VISIBLE : View.GONE);
        if (showStatus) {
            boolean done = s.state == PlanSessionItem.State.DONE;
            b.imgStatus.setImageResource(done ? R.drawable.ic_check_circle_24 : R.drawable.ic_lock_24);
            b.imgStatus.setImageTintList(ColorStateList.valueOf(color(c,
                    done ? R.color.state_success : R.color.text_secondary)));
        }

        // Buổi hôm nay (26): bung danh sách bài
        b.exercises.removeAllViews();
        boolean expand = !s.compact && today;
        b.exercises.setVisibility(expand ? View.VISIBLE : View.GONE);
        if (expand) {
            LayoutInflater inf = LayoutInflater.from(c);
            for (Plan.PlanItem item : s.items) {
                ItemPlanExerciseRowBinding row = ItemPlanExerciseRowBinding.inflate(inf, b.exercises, true);
                row.tvName.setText(item.getExerciseName());
                row.tvVolume.setText(ExerciseUi.volume(c, item.getSets(), item.getReps(), item.isTimeBased()));
                bindImage(row.img, item);
                row.getRoot().setOnClickListener(v -> listener.onExercise(item));
            }
        }
        b.getRoot().setOnClickListener(s.compact || s.state == PlanSessionItem.State.LOCKED ? null
                : v -> { if (!s.items.isEmpty()) listener.onExercise(s.items.get(0)); });
    }

    private static void bindImage(@NonNull ImageView view, @NonNull Plan.PlanItem item) {
        String key = item.getImageName() != null ? item.getImageName() : item.getExerciseId();
        if (key != null) ExerciseImages.bind(view, key);
    }

    private static int color(@NonNull Context c, @ColorRes int res) {
        return ContextCompat.getColor(c, res);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemPlanSessionBinding binding;

        Holder(@NonNull ItemPlanSessionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<PlanSessionItem> DIFF = new DiffUtil.ItemCallback<PlanSessionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull PlanSessionItem a, @NonNull PlanSessionItem b) {
            return a.dayIndex == b.dayIndex;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PlanSessionItem a, @NonNull PlanSessionItem b) {
            return a.equals(b);
        }
    };
}
