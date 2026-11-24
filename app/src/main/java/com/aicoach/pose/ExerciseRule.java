package com.aicoach.pose;

import androidx.annotation.Nullable;

/**
 * Luật chấm của MỘT bài tập (Strategy pattern): mỗi bài tự biết đo cái gì, ngưỡng đếm rep ra sao
 * và lỗi tư thế nào cần bắt. WorkoutEngine dùng chung cho cả 9 bài, chỉ thay luật.
 *
 * Vòng đời mỗi lần lặp: analyze() mọi khung → onRepStarted() khi bắt đầu xuống
 * → (tiếp tục analyze, luật tự ghi nhận số đo xấu nhất) → repIssue() khi hoàn thành rep.
 */
public abstract class ExerciseRule {

    /** Hướng đứng so với camera mà bài này cần để AI chấm chính xác. */
    public enum CameraView { ANY, FRONT, SIDE }

    public final String id;
    public final String name;
    public final int sets;
    /** Số lần mỗi hiệp, hoặc số giây với bài giữ tư thế. */
    public final int target;
    /** Bài tính bằng giây (Plank) thay vì đếm lần. */
    public final boolean timed;
    /** Gợi ý đặt máy, nói ở đầu buổi tập. */
    public final String setupHint;

    protected ExerciseRule(String id, String name, int sets, int target, boolean timed, String setupHint) {
        this.id = id;
        this.name = name;
        this.sets = sets;
        this.target = target;
        this.timed = timed;
        this.setupHint = setupHint;
    }

    /** Đọc một khung hình. Đồng thời cập nhật các số đo "xấu nhất" của lần lặp hiện tại. */
    public abstract FrameAnalysis analyze(PoseFrame f);

    /** Ngưỡng cho bộ đếm rep; null với bài tính giây. */
    @Nullable
    public abstract RepCounter.Config counter();

    /** Hướng đứng cần thiết — màn Kiểm tra sẵn sàng dùng để nhắc người tập. */
    public CameraView preferredView() {
        return CameraView.ANY;
    }

    /** Bài có 2 bộ đếm độc lập (trái/phải) — Nâng gối. */
    public boolean dualCounter() {
        return false;
    }

    /** Gọi khi bắt đầu một lần lặp mới: xoá số đo của lần trước. */
    protected void onRepStarted() {
    }

    /** Lỗi của lần lặp vừa HOÀN THÀNH (đạt độ sâu), null nếu đúng tư thế. */
    @Nullable
    protected FormIssue repIssue() {
        return null;
    }

    /** Lỗi gán cho lần lặp chưa tới (xuống nửa chừng rồi lên). */
    public abstract FormIssue partialIssue();
}
