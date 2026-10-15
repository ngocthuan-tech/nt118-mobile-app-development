package com.aicoach.ui.workout;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.aicoach.R;

/** Trạng thái của 1 mục kiểm tra (khung hình / ánh sáng / hướng đứng) trên màn Kiểm tra sẵn sàng. */
public enum CheckStatus {
    OK(R.drawable.ic_check_24, R.color.state_success),
    WARNING(R.drawable.ic_alert_24, R.color.state_warning),
    ERROR(R.drawable.ic_close_24, R.color.state_error),
    PENDING(0, 0);

    @DrawableRes public final int icon;
    @ColorRes public final int color;

    CheckStatus(@DrawableRes int icon, @ColorRes int color) {
        this.icon = icon;
        this.color = color;
    }

    /** Mục này có cho phép bắt đầu tập không (cảnh báo vẫn cho tập, lỗi/chờ thì không). */
    public boolean isPassing() {
        return this == OK || this == WARNING;
    }
}
