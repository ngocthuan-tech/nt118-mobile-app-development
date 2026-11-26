package com.aicoach.pose;

import androidx.annotation.Nullable;

/** Kết quả WorkoutEngine trả về sau mỗi khung hình: có sự kiện gì mới để giao diện / giọng nói phản ứng. */
public final class WorkoutUpdate {

    public enum Event {
        /** Không có gì mới (chỉ cập nhật số đo, đồng hồ). */
        NONE,
        /** Vừa xong 1 lần đúng tư thế — repNumber là số lần đúng trong hiệp. */
        REP_GOOD,
        /** Vừa xong 1 lần SAI — không được tính, issue cho biết lỗi. */
        REP_BAD,
        /** Bài giữ tư thế: vừa xuất hiện lỗi (ví dụ võng hông khi plank). */
        LIVE_ISSUE,
        /** Bài giữ tư thế: vừa tròn thêm 1 giây đúng. repNumber = số giây trong hiệp. */
        SECOND_TICK,
        /** Xong hiệp (chưa phải hiệp cuối). */
        SET_DONE,
        /** Xong hiệp cuối — hết buổi. */
        WORKOUT_DONE,
        /** Mất dấu người quá 1 giây. */
        BODY_LOST,
        /** Thấy lại người sau khi mất dấu. */
        BODY_FOUND,
    }

    public final Event event;
    @Nullable public final FormIssue issue;
    public final int repNumber;
    public final boolean bodyVisible;
    @Nullable public final String metricText;

    WorkoutUpdate(Event event, @Nullable FormIssue issue, int repNumber, boolean bodyVisible,
                  @Nullable String metricText) {
        this.event = event;
        this.issue = issue;
        this.repNumber = repNumber;
        this.bodyVisible = bodyVisible;
        this.metricText = metricText;
    }
}
