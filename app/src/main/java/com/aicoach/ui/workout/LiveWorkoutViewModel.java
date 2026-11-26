package com.aicoach.ui.workout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aicoach.pose.ExerciseRule;
import com.aicoach.pose.ExerciseRules;
import com.aicoach.pose.FormIssue;
import com.aicoach.pose.PoseFrame;
import com.aicoach.pose.SetResult;
import com.aicoach.pose.WorkoutEngine;
import com.aicoach.pose.WorkoutSummary;
import com.aicoach.pose.WorkoutUpdate;

/**
 * ViewModel màn "Tập với AI Camera" (Figma 30–34).
 * Giữ WorkoutEngine nên tiến độ KHÔNG mất khi sang màn Nghỉ giữa hiệp rồi quay lại
 * (View bị huỷ – tạo lại, còn ViewModel thì sống theo Fragment).
 * Fragment chỉ việc: đưa khung hình vào (onFrame), vẽ state, thực thi command (đọc, chuyển màn).
 */
public class LiveWorkoutViewModel extends ViewModel {

    /** Kiểu phản hồi đang hiển thị dưới bộ đếm. */
    public enum Feedback { NONE, GOOD, BAD, HINT }

    /** Ảnh chụp trạng thái để vẽ giao diện. Bất biến. */
    public static final class State {
        public final String exerciseName;
        public final int currentSet;
        public final int sets;
        public final int progress;
        public final int target;
        public final boolean timed;
        public final long elapsedMs;
        public final int postureScore;
        public final int doneReps;
        /** Tổng số lần đã làm (cả sai) — 0 nghĩa là chưa tập gì. */
        public final int attemptedReps;
        public final int totalTarget;
        @Nullable public final String metricText;
        public final boolean bodyVisible;
        public final Feedback feedback;
        @Nullable public final FormIssue issue;
        public final int feedbackRep;
        public final String coachText;
        @Nullable public final PoseFrame frame;
        @Nullable public final int[] warnJoints;

        State(WorkoutEngine e, @Nullable String metricText, boolean bodyVisible, Feedback feedback,
              @Nullable FormIssue issue, int feedbackRep, String coachText, @Nullable PoseFrame frame) {
            ExerciseRule r = e.rule();
            this.exerciseName = r.name;
            this.currentSet = e.currentSet();
            this.sets = r.sets;
            this.progress = e.setProgress();
            this.target = r.target;
            this.timed = r.timed;
            this.elapsedMs = e.activeMs();
            this.postureScore = e.postureScore();
            this.doneReps = e.correctReps();
            this.attemptedReps = e.totalReps();
            this.totalTarget = r.sets * r.target;
            this.metricText = metricText;
            this.bodyVisible = bodyVisible;
            this.feedback = feedback;
            this.issue = issue;
            this.feedbackRep = feedbackRep;
            this.coachText = coachText;
            this.frame = frame;
            this.warnJoints = issue == null ? null : issue.joints;
        }
    }

    /** Lệnh một lần cho Fragment. */
    public static final class Command {
        public enum Type { SPEAK, GO_REST, GO_RESULT }

        public final Type type;
        @Nullable public final String text;
        public final boolean urgent;
        private boolean handled;

        private Command(Type type, @Nullable String text, boolean urgent) {
            this.type = type;
            this.text = text;
            this.urgent = urgent;
        }

        /** Trả về true đúng 1 lần — tránh chạy lại lệnh khi xoay màn / quay lại từ màn khác. */
        public boolean consume() {
            if (handled) return false;
            handled = true;
            return true;
        }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>();
    private final MutableLiveData<Command> command = new MutableLiveData<>();

    private WorkoutEngine engine;
    private boolean paused;
    /** Tên bài kế tiếp trong buổi giáo án (null = bài cuối / tập lẻ) — để nói "Bài tiếp theo: ...". */
    @Nullable private String nextExerciseName;
    private boolean exerciseClosed;

    // Phản hồi giữ nguyên tới khi có sự kiện mới
    private Feedback feedback = Feedback.HINT;
    @Nullable private FormIssue issue;
    private int feedbackRep;
    private String coachText = "";
    @Nullable private String lastMetricText;
    private boolean bodyVisible;

    public LiveData<State> state() {
        return state;
    }

    public LiveData<Command> command() {
        return command;
    }

    /**
     * Gọi trong onViewCreated. Lần đầu tạo engine; quay lại từ màn Nghỉ thì sang hiệp mới.
     * @param sets   số hiệp theo giáo án (≤ 0 = mặc định của bài)
     * @param target số lần / số giây mỗi hiệp theo giáo án (≤ 0 = mặc định)
     * @param nextExerciseName bài kế tiếp trong buổi giáo án, null nếu không có
     */
    public void onViewReady(@NonNull String exerciseId, int sets, int target, @Nullable String nextExerciseName) {
        this.nextExerciseName = nextExerciseName;
        if (engine == null) {
            engine = new WorkoutEngine(ExerciseRules.create(exerciseId, sets, target));
            coachText = engine.rule().setupHint;
            speak("Bắt đầu " + engine.rule().name + ", hiệp 1", true);
        } else if (engine.isSetFinished() && !engine.isWorkoutFinished()) {
            engine.startNextSet();
            setFeedback(Feedback.HINT, null, 0, "Hiệp " + engine.currentSet() + " – sẵn sàng!");
            speak("Bắt đầu hiệp " + engine.currentSet(), true);
        }
        engine.pauseClock();
        publish(null);
    }

    /** Mỗi khung hình từ camera (main thread). */
    public void onFrame(@NonNull PoseFrame frame) {
        if (engine == null || paused) return;
        WorkoutUpdate u = engine.onFrame(frame, System.currentTimeMillis());
        bodyVisible = u.bodyVisible;
        if (u.metricText != null) lastMetricText = u.metricText;
        ExerciseRule rule = engine.rule();

        switch (u.event) {
            case REP_GOOD:
                setFeedback(Feedback.GOOD, null, u.repNumber, "Tốt lắm, giữ nhịp!");
                speak(String.valueOf(u.repNumber), false);
                break;
            case REP_BAD:
                setFeedback(Feedback.BAD, u.issue, u.repNumber, u.issue == null ? "" : u.issue.voice);
                if (u.issue != null) speak(u.issue.voice, true);
                break;
            case LIVE_ISSUE:
                setFeedback(Feedback.BAD, u.issue, u.repNumber, u.issue == null ? "" : u.issue.voice);
                if (u.issue != null) speak(u.issue.voice, true);
                break;
            case SECOND_TICK:
                if (engine.holdIssue() == null && feedback == Feedback.BAD) {
                    setFeedback(Feedback.GOOD, null, u.repNumber, "Tốt, giữ nguyên!");
                }
                int left = rule.target - u.repNumber;
                if (left > 0 && left <= 3) {
                    speak(String.valueOf(left), false);
                } else if (u.repNumber % 10 == 0) {
                    speak(u.repNumber + " giây", false);
                }
                break;
            case SET_DONE:
                setFeedback(Feedback.GOOD, null, u.repNumber, "Xong hiệp " + engine.currentSet() + "!");
                speak("Tốt lắm! Xong hiệp " + engine.currentSet() + ". Nghỉ ngơi nhé", true);
                command.setValue(new Command(Command.Type.GO_REST, null, false));
                break;
            case WORKOUT_DONE:
                if (nextExerciseName != null) {
                    setFeedback(Feedback.GOOD, null, u.repNumber, "Xong " + rule.name + "! Tiếp theo: " + nextExerciseName);
                    speak("Xong " + rule.name + "! Bài tiếp theo: " + nextExerciseName, true);
                } else {
                    setFeedback(Feedback.GOOD, null, u.repNumber, "Hoàn thành buổi tập!");
                    speak("Hoàn thành buổi tập! Bạn làm rất tốt", true);
                }
                command.setValue(new Command(Command.Type.GO_RESULT, null, false));
                break;
            case BODY_LOST:
                setFeedback(Feedback.HINT, null, 0, "Lùi ra xa để camera thấy toàn thân");
                speak("Lùi ra xa để camera thấy toàn thân", true);
                break;
            case BODY_FOUND:
                setFeedback(Feedback.HINT, null, 0, "Đã thấy bạn, tiếp tục nào!");
                break;
            case NONE:
            default:
                break;
        }
        publish(frame);
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (engine != null) engine.pauseClock();
        publish(null);
    }

    public boolean isPaused() {
        return paused;
    }

    /** Nút "Tập lại hiệp này" trên lớp phủ Tạm dừng. */
    public void redoSet() {
        if (engine == null) return;
        engine.redoSet();
        setFeedback(Feedback.HINT, null, 0, "Tập lại hiệp " + engine.currentSet());
        speak("Tập lại hiệp " + engine.currentSet(), true);
        publish(null);
    }

    /** Kết quả hiệp vừa xong, cho màn Nghỉ giữa hiệp. */
    @Nullable
    public SetResult lastSet() {
        return engine == null ? null : engine.lastSet();
    }

    /**
     * Đánh dấu bài này đã kết thúc (đủ hiệp hoặc bấm Kết thúc). Trả về true đúng 1 lần —
     * tránh lưu trùng nếu người dùng bấm Kết thúc đúng lúc app đang tự chuyển bài.
     */
    public boolean closeExercise() {
        if (exerciseClosed) return false;
        exerciseClosed = true;
        paused = true;
        return true;
    }

    /** Chưa làm lần nào (kể cả lần sai) → kết thúc thì không lưu. */
    public boolean nothingDone() {
        return engine == null || engine.totalReps() == 0;
    }

    /** Tổng kết buổi tập để màn Kết quả hiển thị và gửi POST /sessions. */
    @Nullable
    public WorkoutSummary summary() {
        return engine == null ? null : engine.summary();
    }

    private void setFeedback(Feedback f, @Nullable FormIssue i, int rep, String text) {
        feedback = f;
        issue = i;
        feedbackRep = rep;
        coachText = text;
    }

    private void speak(String text, boolean urgent) {
        command.setValue(new Command(Command.Type.SPEAK, text, urgent));
    }

    private void publish(@Nullable PoseFrame frame) {
        if (engine == null) return;
        state.setValue(new State(engine, lastMetricText, bodyVisible, feedback,
                feedback == Feedback.BAD ? issue : null, feedbackRep, coachText, frame));
    }
}
