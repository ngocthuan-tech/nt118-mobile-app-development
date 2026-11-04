package com.aicoach.ui.profile;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.aicoach.R;
import com.aicoach.domain.BadgeRules;
import com.aicoach.databinding.ItemBadgeBinding;

final class BadgeAdapter extends ListAdapter<BadgeRules.Badge, BadgeAdapter.Holder> {
    BadgeAdapter() {
        super(new DiffUtil.ItemCallback<BadgeRules.Badge>() {
            @Override public boolean areItemsTheSame(@NonNull BadgeRules.Badge a, @NonNull BadgeRules.Badge b) {
                return a.id.equals(b.id);
            }
            @Override public boolean areContentsTheSame(@NonNull BadgeRules.Badge a, @NonNull BadgeRules.Badge b) {
                return a.current == b.current && a.target == b.target;
            }
        });
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(ItemBadgeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        BadgeRules.Badge badge = getItem(position);
        boolean streak = badge.id.startsWith("streak");
        ItemBadgeBinding b = holder.binding;
        b.tvName.setText(holder.itemView.getContext().getString(
                streak ? R.string.profile_badge_streak : R.string.profile_badge_reps, badge.target));
        b.tvDesc.setText(holder.itemView.getContext().getString(R.string.profile_badge_progress,
                Math.min(badge.current, badge.target), badge.target));
        b.progress.setProgress(badge.progress());
        b.progress.setVisibility(badge.earned() ? View.INVISIBLE : View.VISIBLE);
        b.imgLock.setVisibility(badge.earned() ? View.GONE : View.VISIBLE);
        b.imgIcon.setImageResource(streak ? R.drawable.ic_fire_24 : R.drawable.ic_medal_24);
        b.circle.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(),
                badge.earned() ? R.color.state_success : R.color.bg_input_disabled)));
        b.imgIcon.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(holder.itemView.getContext(),
                badge.earned() ? R.color.white : R.color.text_secondary)));
    }
    static class Holder extends RecyclerView.ViewHolder {
        final ItemBadgeBinding binding;
        Holder(ItemBadgeBinding binding) { super(binding.getRoot()); this.binding = binding; }
    }
}
