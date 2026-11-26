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
 * Lớp vẽ khung xương đè lên camera (Figma: "Khung xương AI (ML Kit Pose)").
 * Nhận PoseFrame (toạ độ theo ẢNH camera) rồi tự đổi sang toạ độ View:
 *   - PreviewView dùng scaleType fillCenter → phóng to cho phủ kín rồi cắt mép, căn giữa
 *   - Camera trước được PreviewView lật gương → lật trục x tương ứng
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
    private final int boneNormal;
    private final int boneError;

    private List<Bone> bones = Collections.emptyList();
    private List<PointF> joints = Collections.emptyList();
    private List<PointF> warningJoints = Collections.emptyList();

    public PoseOverlayView(Context context) {
        this(context, null);
    }

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        float density = getResources().getDisplayMetrics().density;
        boneNormal = ContextCompat.getColor(context, R.color.accent_cyan);
        boneError = ContextCompat.getColor(context, R.color.state_error);

        bonePaint.setColor(boneNormal);
        bonePaint.setStrokeWidth(4 * density);
        bonePaint.setStrokeCap(Paint.Cap.ROUND);

        jointPaint.setColor(ContextCompat.getColor(context, R.color.white));
        jointPaint.setStyle(Paint.Style.FILL);

        warningPaint.setColor(boneError);
        warningPaint.setStyle(Paint.Style.STROKE);
        warningPaint.setStrokeWidth(3 * density);
    }

    /**
     * Vẽ khung xương của một khung hình.
     * @param mirror      true với camera trước (hình xem trước bị lật gương)
     * @param warnJoints  các khớp cần khoanh đỏ (lỗi tư thế), có thể null
     */
    public void setPose(@NonNull PoseFrame frame, boolean mirror, @Nullable int[] warnJoints) {
        int vw = getWidth();
        int vh = getHeight();
        if (vw == 0 || vh == 0 || frame.width() == 0 || frame.height() == 0) return;

        float scale = Math.max((float) vw / frame.width(), (float) vh / frame.height());
        float offsetX = (vw - frame.width() * scale) / 2f;
        float offsetY = (vh - frame.height() * scale) / 2f;

        PointF[] pts = new PointF[Landmarks.COUNT];
        for (int i = 0; i < Landmarks.COUNT; i++) {
            if (!frame.visible(i)) continue;
            float x = frame.x(i) * scale + offsetX;
            if (mirror) x = vw - x;
            pts[i] = new PointF(x, frame.y(i) * scale + offsetY);
        }

        List<Bone> b = new ArrayList<>();
        for (int[] pair : Landmarks.BONES) {
            if (pts[pair[0]] != null && pts[pair[1]] != null) b.add(new Bone(pts[pair[0]], pts[pair[1]]));
        }
        List<PointF> j = new ArrayList<>();
        for (int lm : Landmarks.DRAWN_JOINTS) {
            if (pts[lm] != null) j.add(pts[lm]);
        }
        List<PointF> w = new ArrayList<>();
        if (warnJoints != null) {
            for (int lm : warnJoints) {
                if (lm >= 0 && lm < Landmarks.COUNT && pts[lm] != null) w.add(pts[lm]);
            }
        }
        bonePaint.setColor(w.isEmpty() ? boneNormal : boneError);
        setSkeleton(b, j, w);
    }

    /** Cập nhật khung xương đã đổi sẵn sang toạ độ View (gọi trên main thread). */
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
            canvas.drawCircle(p.x, p.y, 5 * density, jointPaint);
        }
        for (PointF p : warningJoints) {
            canvas.drawCircle(p.x, p.y, 16 * density, warningPaint);
        }
    }
}
