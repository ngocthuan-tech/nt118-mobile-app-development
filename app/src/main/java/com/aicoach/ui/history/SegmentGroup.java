package com.aicoach.ui.history;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

/**
 * Nhóm nút dạng "segment" (style Widget.AICoach.Segment) chỉ cho chọn 1 nút một lúc.
 * Dùng chung cho các màn của Thành viên B: Tuần/Tháng/Năm (37), Bạn bè/Toàn quốc (40),
 * 3/7/14/30 ngày (41), bộ lọc Thông báo (22), bộ lọc Huy hiệu (46).
 * MaterialButton không tự giữ trạng thái chọn từ XML nên phải đặt bằng code.
 */
public final class SegmentGroup {

    public interface OnSelect {
        void onSelect(int index);
    }

    private SegmentGroup() {
    }

    /** Chọn nút thứ {@code initial}; bấm nút khác thì đổi trạng thái rồi báo {@code listener} (có thể null). */
    public static void bind(int initial, @Nullable OnSelect listener, MaterialButton... buttons) {
        select(initial, buttons);
        for (int i = 0; i < buttons.length; i++) {
            final int index = i;
            buttons[i].setOnClickListener(v -> {
                select(index, buttons);
                if (listener != null) listener.onSelect(index);
            });
        }
    }

    public static void select(int index, MaterialButton... buttons) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setChecked(i == index);
        }
    }
}
