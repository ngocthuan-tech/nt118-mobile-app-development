package com.aicoach.pose;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bộ não của buổi tập: nhận từng khung hình (PoseFrame), dùng luật của bài (ExerciseRule) để
 * đếm rep / tính giây, chấm đúng–sai, quản lý hiệp và thời gian. Không phụ thuộc Android → unit test được.
 *
 * Luồng: Camera → ML Kit → PoseFrame → WorkoutEngine.onFrame() → WorkoutUpdate → ViewModel → UI + giọng nói.
 */
public final class WorkoutEngine {

    /** Mất dấu người lâu hơn mức này mới báo (tránh báo khi ML Kit trượt 1–2 khung). */
    static final long BODY_LOST_MS = 1000;
    /** Khoảng cách 2 khung lớn hơn mức này (app bị treo, chuyển màn) thì không cộng vào thời gian tập. */
    static final long MAX_FRAME_GAP_MS = 500;
    /** Bài giữ tư thế: lỗi phải kéo dài ngần này khung liên tiếp mới tính (lọc nhiễu). */
    static final int ISSUE_CONFIRM_FRAMES = 5;

    private final ExerciseRule rule;
    private final RepCounter counter;
    private final RepCounter counter2;

    private int currentSet = 1;
    private int setProgress;          // số lần đúng (hoặc số giây đúng) trong hiệp hiện tại
    private int totalReps;            // tất cả các lần (cả sai), hoặc tổng giây giữ tư thế
    private int correctReps;          // lần đúng, hoặc giây đúng
    private boolean setFinished;
    private boolean workoutFinished;
    private final Map<String, Long> formErrors = new LinkedHashMap<>();

    // thống kê riêng hiệp hiện tại (cho màn Nghỉ giữa hiệp)
    private int setAttempts;
    private int setErrors;
    private final Map<FormIssue, Integer> setIssues = new EnumMap<>(FormIssue.class);
    private SetResult lastSet;

    private long startedAtMs = -1;
    private long lastFrameMs = -1;
    private long activeMs;
    private long lostSinceMs = -1;
    private boolean bodyLostReported;

    // bài giữ tư thế
    private long heldMsInSet;
    private long goodMsInSet;
    private long heldMsTotal;
    private long goodMsTotal;
    private FormIssue holdIssue;
    private FormIssue pendingIssue;
    private int pendingFrames;
    private int cleanFrames;

    public WorkoutEngine(ExerciseRule rule) {
        this.rule = rule;
        RepCounter.Config cfg = rule.counter();
        this.counter = cfg == null ? null : new RepCounter(cfg);
        this.counter2 = cfg != null && rule.dualCounter() ? new RepCounter(cfg) : null;
    }

    /**
     * Xử lý một khung hình. {@code wallClockMs} chỉ dùng để ghi giờ bắt đầu buổi (startedAt);
     * thời gian tập tính theo timestamp của khung.
     */
    public WorkoutUpdate onFrame(PoseFrame frame, long wallClockMs) {
        if (startedAtMs < 0) startedAtMs = wallClockMs;
        long t = frame.timeMs();
        long dt = lastFrameMs < 0 ? 0 : t - lastFrameMs;
        lastFrameMs = t;
        if (dt < 0 || dt > MAX_FRAME_GAP_MS) dt = 0;

        FrameAnalysis a = rule.analyze(frame);

        if (setFinished || workoutFinished) {
            return update(WorkoutUpdate.Event.NONE, null, setProgress, a);
        }
        activeMs += dt;

        // ---- Thấy / mất dấu người: phải vắng mặt liên tục quá 1 giây mới báo
        if (!a.visible) {
            if (lostSinceMs < 0) lostSinceMs = t;
            if (!bodyLostReported && t - lostSinceMs > BODY_LOST_MS) {
                bodyLostReported = true;
                if (counter != null) counter.reset();
                if (counter2 != null) counter2.reset();
                return update(WorkoutUpdate.Event.BODY_LOST, null, setProgress, a);
            }
            return update(WorkoutUpdate.Event.NONE, null, setProgress, a);
        }
        lostSinceMs = -1;
        if (bodyLostReported) {
            bodyLostReported = false;
            return update(WorkoutUpdate.Event.BODY_FOUND, null, setProgress, a);
        }

        return rule.timed ? onHoldFrame(a, dt) : onRepFrame(a, t);
    }

    // ------------------------------------------------------------------ bài đếm lần

    private WorkoutUpdate onRepFrame(FrameAnalysis a, long t) {
        RepCounter.Result r1 = counter.update(a.metric, t);
        if (r1 == RepCounter.Result.STARTED) rule.onRepStarted();
        WorkoutUpdate u = handleRepResult(r1, a);
        if (u != null) return u;

        if (counter2 != null) {
            RepCounter.Result r2 = counter2.update(a.metric2, t);
            u = handleRepResult(r2, a);
            if (u != null) return u;
        }
        return update(WorkoutUpdate.Event.NONE, null, setProgress, a);
    }

    private WorkoutUpdate handleRepResult(RepCounter.Result r, FrameAnalysis a) {
        if (r == RepCounter.Result.FULL_REP) {
            FormIssue issue = rule.repIssue();
            return issue == null ? goodRep(a) : badRep(issue, a);
        }
        if (r == RepCounter.Result.PARTIAL_REP) {
            return badRep(rule.partialIssue(), a);
        }
        return null;
    }

    private WorkoutUpdate goodRep(FrameAnalysis a) {
        setAttempts++;
        totalReps++;
        correctReps++;
        setProgress++;
        if (setProgress >= rule.target) return finishSet(a);
        return update(WorkoutUpdate.Event.REP_GOOD, null, setProgress, a);
    }

    private WorkoutUpdate badRep(FormIssue issue, FrameAnalysis a) {
        setAttempts++;
        totalReps++;
        countError(issue);
        return update(WorkoutUpdate.Event.REP_BAD, issue, setProgress, a);
    }

    // ------------------------------------------------------------------ bài giữ tư thế (giây)

    private WorkoutUpdate onHoldFrame(FrameAnalysis a, long dt) {
        if (!a.inPosition) {
            pendingIssue = null;
            pendingFrames = 0;
            holdIssue = null;
            return update(WorkoutUpdate.Event.NONE, null, setProgress, a);
        }

        WorkoutUpdate.Event event = WorkoutUpdate.Event.NONE;
        FormIssue seen = a.liveIssues.isEmpty() ? null : a.liveIssues.iterator().next();

        if (seen == null) {
            pendingIssue = null;
            pendingFrames = 0;
            if (holdIssue != null && ++cleanFrames >= ISSUE_CONFIRM_FRAMES) {
                holdIssue = null;
            }
        } else {
            cleanFrames = 0;
            if (seen == pendingIssue) {
                pendingFrames++;
            } else {
                pendingIssue = seen;
                pendingFrames = 1;
            }
            if (pendingFrames >= ISSUE_CONFIRM_FRAMES && holdIssue != seen) {
                holdIssue = seen;
                countError(seen);
                event = WorkoutUpdate.Event.LIVE_ISSUE;
            }
        }

        heldMsInSet += dt;
        heldMsTotal += dt;
        if (holdIssue == null) {
            goodMsInSet += dt;
            goodMsTotal += dt;
        }
        totalReps = (int) (heldMsTotal / 1000);
        correctReps = (int) (goodMsTotal / 1000);
        setAttempts = (int) (heldMsInSet / 1000);

        int seconds = (int) (goodMsInSet / 1000);
        if (seconds > setProgress) {
            setProgress = seconds;
            if (setProgress >= rule.target) return finishSet(a);
            if (event == WorkoutUpdate.Event.NONE) event = WorkoutUpdate.Event.SECOND_TICK;
        }
        return update(event, event == WorkoutUpdate.Event.LIVE_ISSUE ? holdIssue : null, setProgress, a);
    }

    // ------------------------------------------------------------------ hiệp

    private WorkoutUpdate finishSet(FrameAnalysis a) {
        setFinished = true;
        lastSet = buildSetResult();
        if (currentSet >= rule.sets) {
            workoutFinished = true;
            return update(WorkoutUpdate.Event.WORKOUT_DONE, null, setProgress, a);
        }
        return update(WorkoutUpdate.Event.SET_DONE, null, setProgress, a);
    }

    /** Sau khi nghỉ xong: sang hiệp kế tiếp. */
    public void startNextSet() {
        if (workoutFinished || !setFinished) return;
        currentSet++;
        resetSetState();
    }

    /** Nút "Tập lại hiệp này": bỏ tiến độ hiệp hiện tại (lỗi đã ghi vẫn giữ để thống kê trung thực). */
    public void redoSet() {
        if (workoutFinished) return;
        if (rule.timed) {
            heldMsTotal -= heldMsInSet;
            goodMsTotal -= goodMsInSet;
            totalReps = (int) (heldMsTotal / 1000);
            correctReps = (int) (goodMsTotal / 1000);
        } else {
            correctReps -= setProgress;
        }
        resetSetState();
    }

    private void resetSetState() {
        setProgress = 0;
        setAttempts = 0;
        setErrors = 0;
        setIssues.clear();
        setFinished = false;
        heldMsInSet = 0;
        goodMsInSet = 0;
        holdIssue = null;
        pendingIssue = null;
        pendingFrames = 0;
        if (counter != null) counter.reset();
        if (counter2 != null) counter2.reset();
    }

    /** Gọi khi tạm dừng / rời màn: khung đầu tiên sau đó không bị cộng dồn thời gian chờ. */
    public void pauseClock() {
        lastFrameMs = -1;
        lostSinceMs = -1;
    }

    // ------------------------------------------------------------------ trạng thái

    private void countError(FormIssue issue) {
        Long n = formErrors.get(issue.key);
        formErrors.put(issue.key, n == null ? 1L : n + 1);
        setErrors++;
        Integer m = setIssues.get(issue);
        setIssues.put(issue, m == null ? 1 : m + 1);
    }

    private SetResult buildSetResult() {
        FormIssue top = null;
        int best = 0;
        for (Map.Entry<FormIssue, Integer> e : setIssues.entrySet()) {
            if (e.getValue() > best) {
                best = e.getValue();
                top = e.getKey();
            }
        }
        return new SetResult(currentSet, rule.sets, setProgress, rule.target,
                Math.max(setAttempts, setProgress), setErrors, top, rule.timed);
    }

    /** Kết quả hiệp vừa hoàn thành (null nếu chưa xong hiệp nào). */
    public SetResult lastSet() {
        return lastSet;
    }

    private WorkoutUpdate update(WorkoutUpdate.Event e, FormIssue issue, int rep, FrameAnalysis a) {
        return new WorkoutUpdate(e, issue, rep, a.visible, a.metricText);
    }

    public ExerciseRule rule() {
        return rule;
    }

    public int currentSet() {
        return currentSet;
    }

    public int setProgress() {
        return setProgress;
    }

    public int totalReps() {
        return totalReps;
    }

    public int correctReps() {
        return correctReps;
    }

    public boolean isSetFinished() {
        return setFinished;
    }

    public boolean isWorkoutFinished() {
        return workoutFinished;
    }

    public long activeMs() {
        return activeMs;
    }

    /** Lỗi đang diễn ra của bài giữ tư thế (null nếu đang đúng). */
    public FormIssue holdIssue() {
        return holdIssue;
    }

    /** % lần đúng trên tổng số lần (100 khi chưa có lần nào). */
    public int postureScore() {
        if (totalReps == 0) return 100;
        return Math.round(correctReps * 100f / totalReps);
    }

    public int completedSets() {
        return setFinished ? currentSet : currentSet - 1;
    }

    /** Số hiệp đã tập (tính cả hiệp đang dở nếu đã làm ít nhất 1 lần). */
    public int setsStarted() {
        return setFinished || setAttempts > 0 ? currentSet : currentSet - 1;
    }

    public WorkoutSummary summary() {
        return new WorkoutSummary(rule.id, rule.name, startedAtMs < 0 ? System.currentTimeMillis() : startedAtMs,
                (int) (activeMs / 1000), setsStarted(), rule.sets * rule.target,
                totalReps, correctReps, rule.timed, formErrors);
    }
}
