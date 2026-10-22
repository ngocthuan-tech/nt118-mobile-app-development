package com.aicoach.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.aicoach.R;

/**
 * Ảnh người tập cho 9 bài (Trưởng nhóm — A, B chỉ gọi, không sửa). File: res/drawable-nodpi/img_xxx.webp
 * (img_squat, img_pushup, img_plank, img_lunge, img_glute_bridge, img_dips,
 *  img_jumping_jack, img_high_knee, img_toe_touch) — ảnh người TÁCH NỀN, xuất 3x từ Figma.
 * Chưa có file ảnh thì hiện hình người mờ, app vẫn chạy bình thường.
 * Dùng chung: Trang chủ, Thư viện bài tập, Chi tiết bài tập, Giáo án.
 */
public final class ExerciseImages {

    private ExerciseImages() {
    }

    /** Trả về id ảnh của bài tập, hoặc 0 nếu chưa có file ảnh. */
    @DrawableRes
    public static int find(@NonNull Context context, @NonNull String exerciseIdOrImageName) {
        return context.getResources().getIdentifier(
                imageName(exerciseIdOrImageName), "drawable", context.getPackageName());
    }

    /**
     * Nhận id bài tập ("squat", "chair_dip") hoặc imageName từ API ("img_dips") → tên file ảnh.
     * Hai bài có tên ảnh khác id: chair_dip → img_dips, high_knees → img_high_knee.
     */
    @NonNull
    static String imageName(@NonNull String key) {
        if (key.startsWith("img_")) return key;
        switch (key) {
            case "chair_dip": return "img_dips";
            case "high_knees": return "img_high_knee";
            default: return "img_" + key;
        }
    }

    /** Gắn ảnh bài tập vào ImageView (nền xanh đậm đã đặt sẵn trong layout bằng bg_exercise_photo). */
    public static void bind(@NonNull ImageView view, @NonNull String exerciseIdOrImageName) {
        int res = find(view.getContext(), exerciseIdOrImageName);
        if (res != 0) {
            view.setImageResource(res);
            view.setImageTintList(null);
            view.setPadding(0, 0, 0, 0);
        } else {
            int pad = view.getResources().getDimensionPixelSize(R.dimen.space_16);
            view.setImageResource(R.drawable.ic_body_24);
            view.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(view.getContext(), R.color.white_40)));
            view.setPadding(pad, pad, pad, pad);
        }
    }
}
