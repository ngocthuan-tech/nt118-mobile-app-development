package com.aicoach.pose;

import java.util.EnumSet;
import java.util.Set;

/** Kết quả một bài tập "đọc" được từ một khung hình. */
public final class FrameAnalysis {

    /** Đủ các khớp cần thiết để chấm bài này. */
    public final boolean visible;
    /** Số đo chính cho bộ đếm rep (NaN = khung này không đo được). */
    public final double metric;
    /** Số đo phụ cho bộ đếm thứ hai (chỉ bài Nâng gối dùng: chân phải). */
    public final double metric2;
    /** Chữ hiện trên chip, ví dụ "Góc gối 94°". */
    public final String metricText;
    /** Bài giữ tư thế (Plank): người đã vào đúng dáng nằm chưa. */
    public final boolean inPosition;
    /** Lỗi đang thấy NGAY khung này (bài giữ tư thế dùng). */
    public final Set<FormIssue> liveIssues;

    private FrameAnalysis(boolean visible, double metric, double metric2, String metricText,
                          boolean inPosition, Set<FormIssue> liveIssues) {
        this.visible = visible;
        this.metric = metric;
        this.metric2 = metric2;
        this.metricText = metricText;
        this.inPosition = inPosition;
        this.liveIssues = liveIssues;
    }

    public static FrameAnalysis notVisible() {
        return new FrameAnalysis(false, Double.NaN, Double.NaN, null, false, EnumSet.noneOf(FormIssue.class));
    }

    public static FrameAnalysis of(double metric, String metricText) {
        return new FrameAnalysis(true, metric, Double.NaN, metricText, true, EnumSet.noneOf(FormIssue.class));
    }

    public static FrameAnalysis dual(double left, double right, String metricText) {
        return new FrameAnalysis(true, left, right, metricText, true, EnumSet.noneOf(FormIssue.class));
    }

    public static FrameAnalysis hold(boolean inPosition, String metricText, Set<FormIssue> liveIssues) {
        return new FrameAnalysis(true, Double.NaN, Double.NaN, metricText, inPosition,
                liveIssues == null ? EnumSet.noneOf(FormIssue.class) : liveIssues);
    }
}
