package com.aicoach.api.seed;

import com.aicoach.api.common.VnTime;
import com.aicoach.api.exercise.Exercise;
import com.aicoach.api.exercise.ExerciseRepository;
import com.aicoach.api.plan.Plan;
import com.aicoach.api.plan.PlanRepository;
import com.aicoach.api.session.SessionService;
import com.aicoach.api.session.SessionService.CreateSessionRequest;
import com.aicoach.api.user.UserAccount;
import com.aicoach.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Nạp dữ liệu nền: 9 bài tập, 3 giáo án (ghi đè mỗi lần khởi động).
 * Bật app.seed.demo thì tạo thêm tài khoản demo@aicoach.vn / Demo1234 có sẵn lịch sử tập để demo Trang chủ.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    public static final String DEMO_EMAIL = "demo@aicoach.vn";
    public static final String DEMO_PASSWORD = "Demo1234";

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Tên ảnh trong app (res/drawable-nodpi) — khớp docs/MAN_HINH.md. */
    private static final Map<String, String> IMAGE_NAMES = Map.of(
            "chair_dip", "img_dips",
            "high_knees", "img_high_knee");

    private final ExerciseRepository exercises;
    private final PlanRepository plans;
    private final UserRepository users;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final boolean seedDemo;

    public DataSeeder(ExerciseRepository exercises, PlanRepository plans, UserRepository users,
                      SessionService sessionService, PasswordEncoder passwordEncoder, Clock clock,
                      @Value("${app.seed.demo:true}") boolean seedDemo) {
        this.exercises = exercises;
        this.plans = plans;
        this.users = users;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.seedDemo = seedDemo;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Dữ liệu tham chiếu (bài tập, giáo án) luôn được ghi đè theo code mỗi lần khởi động,
        // nhờ vậy sửa nội dung chỉ cần deploy lại, không phải xoá database.
        seedExercises();
        seedPlans();
        log.info("Đã đồng bộ 9 bài tập và 3 giáo án");
        if (seedDemo && !users.existsByEmail(DEMO_EMAIL)) {
            seedDemoUser();
            log.info("Đã tạo tài khoản demo {}", DEMO_EMAIL);
        }
    }

    // ------------------------------------------------------------------ Bài tập

    private void seedExercises() {
        int order = 0;
        exercises.save(ex(order++, "squat", "Squat", "legs", "easy", "reps", 3, 15, 0.32,
                "Bài tập nền tảng cho đùi và mông, AI theo dõi góc gối và độ thẳng lưng.",
                List.of("Đứng thẳng, hai chân rộng bằng vai, mũi chân hơi mở ra ngoài",
                        "Đẩy hông ra sau và hạ người như ngồi xuống ghế",
                        "Hạ đến khi đùi song song mặt sàn rồi đạp gót đứng lên"),
                List.of("Giữ ngực mở, mắt nhìn thẳng", "Dồn lực vào gót chân"),
                List.of("Đầu gối đổ vào trong", "Gù lưng khi xuống thấp")));
        exercises.save(ex(order++, "pushup", "Hít đất", "chest_arms", "medium", "reps", 3, 12, 0.36,
                "Tăng sức mạnh ngực, vai và tay sau. AI kiểm tra độ thẳng của thân người.",
                List.of("Chống hai tay rộng hơn vai một chút, thân người thẳng từ đầu đến gót",
                        "Gập khuỷu tay hạ ngực xuống gần sàn",
                        "Đẩy mạnh lên về tư thế ban đầu"),
                List.of("Siết cơ bụng suốt động tác", "Mới tập có thể chống gối"),
                List.of("Võng hông xuống sàn", "Chỉ hạ nửa chừng")));
        exercises.save(ex(order++, "plank", "Plank", "core", "easy", "seconds", 3, 45, 0.08,
                "Giữ tư thế tĩnh để siết cơ bụng và lưng dưới. Tính theo số giây giữ đúng tư thế.",
                List.of("Chống hai khuỷu tay ngay dưới vai",
                        "Duỗi thẳng chân, kiễng mũi chân",
                        "Giữ thân người thành một đường thẳng"),
                List.of("Thở đều, không nín thở", "Nhìn xuống sàn để cổ thẳng"),
                List.of("Nhô mông quá cao", "Võng lưng")));
        exercises.save(ex(order++, "lunge", "Lunge", "legs", "medium", "reps", 3, 12, 0.30,
                "Bài một chân giúp đùi, mông khoẻ và cải thiện thăng bằng.",
                List.of("Đứng thẳng, bước một chân lên phía trước",
                        "Hạ người đến khi hai gối gập khoảng 90 độ",
                        "Đạp chân trước để trở về, đổi chân"),
                List.of("Thân trên giữ thẳng", "Bước đủ dài để gối không vượt mũi chân"),
                List.of("Gối trước vượt quá mũi chân", "Gối sau đập xuống sàn")));
        exercises.save(ex(order++, "glute_bridge", "Cầu mông", "legs", "easy", "reps", 3, 15, 0.25,
                "Tập trung vào cơ mông và đùi sau, nhẹ cho khớp gối.",
                List.of("Nằm ngửa, co gối, bàn chân đặt sát mông",
                        "Siết mông nâng hông lên đến khi thân thẳng từ vai đến gối",
                        "Giữ 1 giây rồi hạ xuống chậm"),
                List.of("Đẩy lực bằng gót chân", "Siết mông ở điểm cao nhất"),
                List.of("Ưỡn lưng thay vì dùng mông", "Hạ người quá nhanh")));
        exercises.save(ex(order++, "chair_dip", "Chống ghế", "chest_arms", "medium", "reps", 3, 10, 0.30,
                "Dùng ghế chắc chắn để tập tay sau và vai.",
                List.of("Ngồi mép ghế, hai tay chống cạnh hông",
                        "Trượt mông ra khỏi ghế, gập khuỷu tay hạ người xuống",
                        "Đẩy tay duỗi thẳng để nâng người lên"),
                List.of("Khuỷu tay hướng ra sau", "Dùng ghế không có bánh xe"),
                List.of("Mở khuỷu tay sang hai bên", "Nhún vai lên tai")));
        exercises.save(ex(order++, "jumping_jack", "Bật nhảy", "cardio", "easy", "reps", 3, 30, 0.20,
                "Bài khởi động toàn thân, tăng nhịp tim nhanh.",
                List.of("Đứng thẳng, hai tay xuôi theo người",
                        "Bật nhảy dang hai chân, đồng thời vung tay qua đầu",
                        "Bật nhảy về tư thế ban đầu"),
                List.of("Tiếp đất nhẹ bằng mũi chân", "Giữ nhịp đều"),
                List.of("Tay không đưa lên hết", "Tiếp đất nặng bằng gót")));
        exercises.save(ex(order++, "high_knees", "Nâng gối", "cardio", "easy", "reps", 3, 30, 0.18,
                "Chạy tại chỗ nâng cao gối, đốt calo và tăng sức bền.",
                List.of("Đứng thẳng, hai tay để trước bụng",
                        "Nâng cao gối trái chạm tay rồi hạ xuống",
                        "Đổi gối phải, lặp lại liên tục"),
                List.of("Nâng gối ngang hông", "Đánh tay theo nhịp"),
                List.of("Ngả người ra sau", "Gối nâng quá thấp")));
        exercises.save(ex(order, "toe_touch", "Chạm chân", "core", "easy", "reps", 3, 15, 0.15,
                "Gập người chạm mũi chân, kéo giãn đùi sau và siết cơ bụng.",
                List.of("Đứng thẳng, hai chân rộng bằng hông",
                        "Gập người từ hông, đưa tay chạm mũi chân",
                        "Từ từ cuộn người đứng thẳng lại"),
                List.of("Hơi chùng gối nếu chưa dẻo", "Thở ra khi gập người"),
                List.of("Giật người quá nhanh", "Khóa cứng đầu gối")));
    }

    private static Exercise ex(int order, String id, String name, String muscle, String difficulty, String measure,
                               int sets, int reps, double kcal, String description,
                               List<String> steps, List<String> tips, List<String> mistakes) {
        Exercise e = new Exercise();
        e.setSortOrder(order);
        e.setId(id);
        e.setName(name);
        e.setMuscleGroup(muscle);
        e.setDifficulty(difficulty);
        e.setImageName(IMAGE_NAMES.getOrDefault(id, "img_" + id));
        e.setMeasureType(measure);
        e.setDefaultSets(sets);
        e.setDefaultReps(reps);
        e.setCaloriesPerRep(kcal);
        e.setDescription(description);
        e.setSteps(new java.util.ArrayList<>(steps));
        e.setTips(new java.util.ArrayList<>(tips));
        e.setCommonMistakes(new java.util.ArrayList<>(mistakes));
        return e;
    }

    // ------------------------------------------------------------------ Giáo án

    private void seedPlans() {
        Plan fatBurn = plan(0, "fat-burn-4w", "Đốt mỡ toàn thân – 4 tuần",
                "Kết hợp cardio và sức mạnh, 3 buổi mỗi tuần cho người mới bắt đầu.",
                "beginner", 28, 3, 25, "img_jumping_jack");
        for (int i = 0; i < 12; i++) {
            int day = i + 1;
            switch (i % 3) {
                case 0 -> {
                    fatBurn.addItem(day, "jumping_jack", 3, 30);
                    fatBurn.addItem(day, "squat", 3, 15);
                    fatBurn.addItem(day, "high_knees", 3, 30);
                    fatBurn.addItem(day, "plank", 3, 30);
                }
                case 1 -> {
                    fatBurn.addItem(day, "squat", 3, 15);
                    fatBurn.addItem(day, "pushup", 3, 10);
                    fatBurn.addItem(day, "glute_bridge", 3, 15);
                    fatBurn.addItem(day, "toe_touch", 3, 15);
                }
                default -> {
                    fatBurn.addItem(day, "jumping_jack", 3, 30);
                    fatBurn.addItem(day, "lunge", 3, 10);
                    fatBurn.addItem(day, "chair_dip", 3, 10);
                    fatBurn.addItem(day, "plank", 3, 40);
                }
            }
        }
        plans.save(fatBurn);

        Plan strength = plan(1, "strength-4w", "Sức mạnh cơ bản – 4 tuần",
                "Tăng sức mạnh chân, ngực, tay với số rep tăng dần.",
                "intermediate", 28, 3, 30, "img_pushup");
        for (int i = 0; i < 12; i++) {
            int day = i + 1;
            switch (i % 3) {
                case 0 -> {
                    strength.addItem(day, "squat", 4, 15);
                    strength.addItem(day, "lunge", 3, 12);
                    strength.addItem(day, "glute_bridge", 3, 20);
                }
                case 1 -> {
                    strength.addItem(day, "pushup", 4, 12);
                    strength.addItem(day, "chair_dip", 3, 12);
                    strength.addItem(day, "plank", 3, 45);
                }
                default -> {
                    strength.addItem(day, "squat", 3, 20);
                    strength.addItem(day, "pushup", 3, 12);
                    strength.addItem(day, "plank", 3, 60);
                }
            }
        }
        plans.save(strength);

        Plan core = plan(2, "core-2w", "Bụng săn chắc – 2 tuần",
                "15 phút mỗi buổi, 4 buổi mỗi tuần, tập trung cơ bụng và lưng.",
                "beginner", 14, 4, 15, "img_plank");
        for (int i = 0; i < 8; i++) {
            int day = i + 1;
            if (i % 2 == 0) {
                core.addItem(day, "plank", 3, 30);
                core.addItem(day, "toe_touch", 3, 15);
                core.addItem(day, "glute_bridge", 3, 15);
            } else {
                core.addItem(day, "high_knees", 3, 30);
                core.addItem(day, "plank", 3, 40);
                core.addItem(day, "toe_touch", 3, 20);
            }
        }
        plans.save(core);
    }

    private static Plan plan(int order, String id, String title, String description, String level,
                             int days, int perWeek, int minutes, String cover) {
        Plan p = new Plan();
        p.setSortOrder(order);
        p.setId(id);
        p.setTitle(title);
        p.setDescription(description);
        p.setLevel(level);
        p.setDurationDays(days);
        p.setSessionsPerWeek(perWeek);
        p.setMinutesPerSession(minutes);
        p.setCoverImageName(cover);
        return p;
    }

    // ------------------------------------------------------------------ Tài khoản demo

    private void seedDemoUser() {
        LocalDate today = VnTime.today(clock);
        UserAccount u = new UserAccount();
        u.setEmail(DEMO_EMAIL);
        u.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        u.setDisplayName("Minh");
        u.setHeightCm(170.0);
        u.setWeightKg(65.0);
        u.setGoal("lose_weight");
        u.setLevel("beginner");
        u.setWeeklyGoalSessions(4);
        u.setProfileCompleted(true);
        u.setActivePlanId("fat-burn-4w");
        u.setPlanStartDate(today.minusDays(11));
        users.saveAndFlush(u);

        // 8 buổi theo giáo án trong 10 ngày qua, 5 ngày gần nhất liên tiếp → streak 5.
        int[] daysAgo = {10, 8, 7, 5, 4, 3, 2, 1};
        String[][] pairs = {
                {"jumping_jack", "squat"}, {"squat", "pushup"}, {"lunge", "plank"},
                {"jumping_jack", "high_knees"}, {"squat", "glute_bridge"}, {"lunge", "chair_dip"},
                {"jumping_jack", "squat"}, {"pushup", "toe_touch"}};
        for (int i = 0; i < daysAgo.length; i++) {
            Instant start = today.minusDays(daysAgo[i]).atTime(LocalTime.of(19, 0)).atZone(VnTime.ZONE).toInstant();
            for (int j = 0; j < 2; j++) {
                String exerciseId = pairs[i][j];
                int target = "plank".equals(exerciseId) ? 90 : 45;
                int total = target - (i % 3) * 3;
                int correct = total - 2 - (i + j) % 4;
                Map<String, Long> errors = correct < total
                        ? Map.of("knee_over_toe", (long) (total - correct))
                        : Map.of();
                sessionService.create(u.getId(), new CreateSessionRequest(exerciseId,
                        start.plusSeconds(j * 420L), 360, target, total, correct, null, errors, "fat-burn-4w"));
            }
        }
    }
}
