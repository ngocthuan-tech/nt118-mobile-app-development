package com.aicoach.ui.workout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aicoach.R;
import com.aicoach.databinding.ItemAiFeedbackBinding;

/** Danh sách nhận xét của AI sau buổi tập. Lỗi nhẹ = vàng, lỗi nặng = đỏ, làm tốt = xanh. */
public class AiFeedbackAdapter extends ListAdapter<AiFeedbackItem, AiFeedbackAdapter.ViewHolder> {

    public AiFeedbackAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAiFeedbackBinding b = ItemAiFeedbackBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AiFeedbackItem item = getItem(position);
        Context ctx = holder.binding.getRoot().getContext();
        boolean good = item.type == AiFeedbackItem.Type.GOOD;
        int accentRes;
        int accentLightRes;
        switch (item.type) {
            case GOOD:
                accentRes = R.color.state_success;
                accentLightRes = R.color.state_success_light;
                break;
            case ERROR:
                accentRes = R.color.state_error;
                accentLightRes = R.color.state_error_light;
                break;
            default:
                accentRes = R.color.state_warning;
                accentLightRes = R.color.state_warning_light;
                break;
        }
        int accent = ContextCompat.getColor(ctx, accentRes);
        int accentLight = ContextCompat.getColor(ctx, accentLightRes);

        holder.binding.viewAccent.setBackgroundColor(accent);
        holder.binding.iconBg.setBackgroundTintList(ColorStateList.valueOf(accentLight));
        holder.binding.imgIcon.setImageResource(good ? R.drawable.ic_check_24 : R.drawable.ic_alert_24);
        holder.binding.imgIcon.setImageTintList(ColorStateList.valueOf(accent));
        holder.binding.tvFeedbackTitle.setText(item.title);
        holder.binding.tvFeedbackDetail.setText(item.detail);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemAiFeedbackBinding binding;

        ViewHolder(@NonNull ItemAiFeedbackBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<AiFeedbackItem> DIFF =
            new DiffUtil.ItemCallback<AiFeedbackItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull AiFeedbackItem a, @NonNull AiFeedbackItem b) {
                    return a.title.equals(b.title);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AiFeedbackItem a, @NonNull AiFeedbackItem b) {
                    return a.equals(b);
                }
            };
}
