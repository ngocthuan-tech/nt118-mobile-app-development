package com.aicoach.pose;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.aicoach.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp vẽ khung xương đè lên camera (Figma 12: "Khung xương AI (ML Kit Pose)").
 * Nhận toạ độ các khớp ĐÃ đổi sang hệ toạ độ của View, vẽ đường nối + chấm khớp.
 * Việc đổi toạ độ từ ảnh camera sang View do lớp phân tích tư thế đảm nhận (Tuần 4).
 */
public class PoseOverlayView extends View {

    /** Một đoạn nối giữa 2 khớp, ví dụ hông → gối. */
    public static final class Bone {
        public final PointF from;
        public final PointF to;

        public Bone(@NonNull PointF from, @NonNull PointF to) {
            this.from = from;
            this.to = to;
        }
    }

    private final Paint bonePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint jointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint warningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private List<Bone> bones = Collections.emptyList();
    private List<PointF> joints = Collections.emptyList();
    private List<PointF> warningJoints = Collections.emptyList();

    public PoseOverlayView(Context context) {
        this(context, null);
    }

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;

        bonePaint.setColor(ContextCompat.getColor(context, R.color.white));
        bonePaint.setStrokeWidth(4 * density);
        bonePaint.setStrokeCap(Paint.Cap.ROUND);

        jointPaint.setColor(ContextCompat.getColor(context, R.color.brand_primary));
        jointPaint.setStyle(Paint.Style.FILL);

        warningPaint.setColor(ContextCompat.getColor(context, R.color.state_error));
        warningPaint.setStyle(Paint.Style.STROKE);
        warningPaint.setStrokeWidth(3 * density);
    }

    /** Cập nhật khung xương mới (gọi trên main thread mỗi khung hình). */
    public void setSkeleton(@NonNull List<Bone> bones, @NonNull List<PointF> joints,
                            @NonNull List<PointF> warningJoints) {
        this.bones = new ArrayList<>(bones);
        this.joints = new ArrayList<>(joints);
        this.warningJoints = new ArrayList<>(warningJoints);
        invalidate();
    }

    public void clear() {
        bones = Collections.emptyList();
        joints = Collections.emptyList();
        warningJoints = Collections.emptyList();
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        for (Bone b : bones) {
            canvas.drawLine(b.from.x, b.from.y, b.to.x, b.to.y, bonePaint);
        }
        for (PointF p : joints) {
            canvas.drawCircle(p.x, p.y, 6 * density, jointPaint);
        }
        for (PointF p : warningJoints) {
            canvas.drawCircle(p.x, p.y, 14 * density, warningPaint);
        }
    }
}
