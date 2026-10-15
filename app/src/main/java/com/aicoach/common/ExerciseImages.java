package com.aicoach.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.aicoach.R;

/**
 * Ảnh người tập cho 9 bài. Quy ước tên file: res/drawable-nodpi/img_{exerciseId}.webp
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
    public static int find(@NonNull Context context, @NonNull String exerciseId) {
        // Tên ảnh lấy theo quy ước (giống field imageName của Exercise trên Firestore)
        return context.getResources().getIdentifier("img_" + exerciseId, "drawable", context.getPackageName());
    }

    /** Gắn ảnh bài tập vào ImageView (nền xanh đậm đã đặt sẵn trong layout bằng bg_exercise_photo). */
    public static void bind(@NonNull ImageView view, @NonNull String exerciseId) {
        int res = find(view.getContext(), exerciseId);
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
