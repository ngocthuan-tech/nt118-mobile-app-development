---
title: "AI Coach — Phân công Thành viên B (Tiến độ & Cộng đồng)"
date: "15/10/2026"
---

# 1. Vai trò và phạm vi

Thành viên B phụ trách module **Tiến độ & Cộng đồng**: mọi thứ xảy ra *sau* khi người dùng tập xong — lưu buổi tập, lịch sử, biểu đồ, streak, XP, huy hiệu, bạn bè, thử thách và bảng xếp hạng. Phần đăng nhập, trang chủ, thư viện bài tập do Thành viên A làm; phần camera AI do Trưởng nhóm làm.

| Mã | Màn hình | Độ khó | Package |
|---|---|---|---|
| S20 | Lịch sử tập (danh sách buổi, nhóm theo ngày) | Trung bình | ui.history |
| S21 | Chi tiết buổi tập (rep đúng/sai, lỗi tư thế, thời gian, calo) | Trung bình | ui.history |
| S22 | Thống kê (biểu đồ 7 ngày / 30 ngày) | Khó | ui.stats |
| S23 | Huy hiệu & thành tích | Trung bình | ui.stats |
| S24 | Bạn bè (danh sách + tìm bạn) | Trung bình | ui.social |
| S25 | Lời mời kết bạn | Trung bình | ui.social |
| S26 | Danh sách thử thách (đang diễn ra / đã kết thúc) | Trung bình | ui.challenge |
| S27 | Tạo thử thách | Trung bình | ui.challenge |
| S28 | Chi tiết thử thách + xếp hạng trong thử thách | Khó | ui.challenge |
| S29 | Bảng xếp hạng bạn bè theo XP tuần | Trung bình | ui.social |

Ngoài màn hình, B sở hữu **lớp nghiệp vụ** `ProgressService` (tính streak, XP, huy hiệu, cộng điểm thử thách) — đây là phần logic để ghi điểm "có thuật toán" trước hội đồng.

**Điểm giao với người khác** (bắt buộc giữ đúng):

- Màn Kết quả (S18) của Trưởng nhóm tạo một object `WorkoutSession` rồi gọi `SessionRepository.saveCompletedSession(session)`. B viết hàm này; trong đó lưu buổi tập rồi gọi `ProgressService` cập nhật mọi thứ.
- B là người duy nhất ghi `streak`, `weeklyXp`, `weekKey`, `totalXp`, `totalReps`, `lastWorkoutDate` trong `users/{uid}`. Trang chủ của A chỉ đọc.
- Từ S21, nút "Tập lại" gọi `action_global_to_readiness` kèm `exerciseId`.

Nếu tên frame trong Figma khác bảng trên, đổi tên frame Figma theo mã S20–S29 để cả nhóm nói cùng một ngôn ngữ. Mã S15–S19 là của Trưởng nhóm (kiểm tra khung hình, tập trực tiếp, nghỉ, kết quả).

# 2. Chuẩn chung toàn nhóm (cả 3 người làm giống hệt nhau)

Cả nhóm code **Java + XML View**, kiến trúc **MVVM**, một Activity duy nhất (`MainActivity`) chứa các Fragment điều hướng bằng **Navigation Component**. Trưởng nhóm dựng sẵn khung project ở Tuần 1; không ai tự tạo Activity mới hay thêm thư viện khi chưa báo nhóm.

**Thư viện thống nhất** (Trưởng nhóm khai báo trong `build.gradle`, đừng tự đổi phiên bản):

- Material Components, ConstraintLayout, Navigation, ViewModel + LiveData, ViewBinding
- Firebase: Auth, Firestore, Storage (BoM)
- Glide (tải ảnh), **MPAndroidChart** (biểu đồ – B dùng chính)
- CameraX + ML Kit Pose Detection (module AI – Trưởng nhóm dùng)

**Cấu trúc package** (`com.aicoach`):

```
com.aicoach
├── data
│   ├── model        // User, Exercise, Plan, WorkoutSession, Challenge, Badge
│   ├── repository   // UserRepository, SessionRepository, SocialRepository, ChallengeRepository
│   └── local        // SettingsPrefs (SharedPreferences)
├── domain           // ProgressService, BadgeRules  (Thành viên B)
├── ui
│   ├── auth  home  exercise  plan  profile      // Thành viên A
│   ├── workout                                 // Trưởng nhóm
│   └── history  stats  social  challenge       // Thành viên B
├── pose             // Trưởng nhóm: analyzer, rep counter, rules
├── common           // Resource<T>, BaseFragment, Constants, FirestorePaths
└── util             // DateUtils, ValidationUtils, ...
```

**Luồng dữ liệu bắt buộc**: Fragment → ViewModel → Repository (→ ProgressService nếu có tính toán) → Firebase. Fragment không bao giờ gọi `FirebaseFirestore.getInstance()` trực tiếp. Mọi kết quả trả về bọc trong `Resource<T>` (LOADING / SUCCESS / ERROR).

**Quy tắc đặt tên**

| Loại | Mẫu | Ví dụ |
|---|---|---|
| Fragment | `<Tên>Fragment` | `HistoryFragment`, `ChallengeDetailFragment` |
| ViewModel | `<Tên>ViewModel` | `StatsViewModel` |
| Layout màn hình | `fragment_<tên>.xml` | `fragment_challenge_detail.xml` |
| Layout 1 dòng list | `item_<tên>.xml` | `item_session.xml`, `item_leaderboard.xml` |
| Icon | `ic_<tên>_24.xml` | `ic_trophy_24.xml` |
| Nền/shape | `bg_<tên>.xml` | `bg_badge_locked.xml` |
| String | `<module>_<tên>` | `history_empty_title`, `challenge_create_button` |
| View id | `camelCase` theo vai trò | `rvSessions`, `chartWeeklyReps`, `tvStreak` |

Không hardcode chữ, màu, kích thước trong XML: chữ vào `strings.xml`, màu vào `colors.xml`, khoảng cách vào `dimens.xml`. Mỗi người chỉ thêm string/màu có tiền tố module của mình để tránh xung đột.

**Điều hướng không đụng nhau**: mỗi module có file graph riêng (`nav_auth.xml`, `nav_main.xml` cho A; `nav_workout.xml` cho Trưởng nhóm; `nav_social.xml` cho toàn bộ S20–S29 của B), được `include` vào `nav_root.xml` do Trưởng nhóm giữ.

**Git (GitHub)**

- Nhánh: `main` (bản nộp) ← `develop` ← `feature/<tên-bạn>-<mã-màn>` (vd. `feature/b-s22-stats`).
- Không push thẳng vào `main`/`develop`. Mở Pull Request, Trưởng nhóm review rồi merge.
- Commit nhỏ, mô tả rõ: `feat(progress): update streak after session`, `fix(stats): empty chart crash`.
- Pull `develop` mỗi sáng trước khi code. Hội đồng có thể mở lịch sử commit để xem ai làm gì — commit đều đặn là bằng chứng đóng góp.

# 3. Chuyển Figma sang XML trong Android Studio

Figma chỉ là bản vẽ: mình tự dựng lại bằng XML theo số đo trong Figma, không dùng plugin tự sinh code (code sinh ra dùng tọa độ tuyệt đối, vỡ layout trên máy khác và bị hội đồng đánh giá thấp).

1. **Mở Figma ở chế độ Dev Mode / Inspect.** Bấm vào từng phần tử để đọc kích thước, padding, bo góc, màu, font. Frame Figma rộng 360 thì 1 px = 1 dp; nếu frame rộng 390/393 (khung iPhone), vẫn đọc số như dp và kiểm tra lại trên máy thật.
2. **Không tự thêm màu/font.** Trưởng nhóm đã đưa toàn bộ bảng màu vào `colors.xml`, kiểu chữ vào `themes.xml` (`TextAppearance.AICoach.Title`, `.Body`, `.Caption`), khoảng cách vào `dimens.xml` (`space_8`, `space_16`, `radius_card`...). Thiếu thì báo Trưởng nhóm thêm.
3. **Xuất icon dạng SVG** → Android Studio: `File > New > Vector Asset > Local file` → đặt tên `ic_<tên>_24`. Icon huy hiệu do B quản lý: mỗi huy hiệu một vector, bản "chưa đạt" dùng cùng file nhưng tô xám bằng `setColorFilter` hoặc `alpha`, không vẽ hai bản.
4. **Xuất ảnh dạng WebP**, khoảng 1080 px chiều dài nhất, đặt trong `res/drawable-nodpi/` với tên `img_<tên>.webp`.
5. **Dựng layout bằng `ConstraintLayout`** cho màn phức tạp, `LinearLayout` cho khối đơn giản. Dùng sẵn component Material (`MaterialButton`, `MaterialCardView`, `Chip`, `TabLayout`, `MaterialButtonToggleGroup`) để giống Figma mà ít code.
6. **Component lặp lại trong Figma = 1 file XML dùng chung**: dòng lịch sử → `item_session.xml`; dòng xếp hạng → `item_leaderboard.xml`; ô thống kê nhỏ → `view_stat_box.xml` (dùng chung với A, ai làm trước thì tạo, người sau dùng lại).
7. **Danh sách luôn dùng `RecyclerView` + `ListAdapter` + `DiffUtil`**, không dùng `ListView`, không thêm view bằng vòng lặp.
8. **Biểu đồ**: Figma chỉ là ảnh minh họa; dựng bằng `BarChart`/`LineChart` của MPAndroidChart rồi chỉnh màu, font, lưới cho giống Figma (tắt lưới dọc, tắt mô tả, bo cột).
9. **Kiểm tra**: chạy trên 1 máy màn nhỏ (5 inch) và 1 máy lớn, bật Dark mode thử, xoay ngang thử. Chụp màn hình đặt cạnh Figma rồi gửi vào nhóm khi mở Pull Request.

Quy trình mỗi màn: **Tuần dựng UI** chỉ làm XML + dữ liệu giả để giống Figma; **tuần sau** mới nối ViewModel + Firebase. Tách như vậy để không bị kẹt khi Firebase hoặc module AI chưa sẵn sàng — B tự tạo vài `WorkoutSession` giả để thử.

# 4. Dữ liệu và luật nghiệp vụ

**Firestore B làm việc cùng** (khuôn do Trưởng nhóm chốt, không tự thêm field):

```
users/{uid}                      // B ghi các field tiến độ
  streak, lastWorkoutDate ("yyyy-MM-dd"), weeklyXp, weekKey ("2026-W43"),
  totalXp, totalReps, emailLower, displayName, avatarUrl

users/{uid}/sessions/{sessionId}
  exerciseId, exerciseName, startedAt (Timestamp), durationSec,
  targetReps, totalReps, correctReps, accuracy (0..1), calories,
  formErrors { "knee_over_toe": 3, "back_not_straight": 1 }, planId?

users/{uid}/badges/{badgeId}     // earnedAt
users/{uid}/friends/{friendUid}  // since

friendRequests/{id}
  fromUid, toUid, status ("pending" | "accepted" | "declined"), createdAt

challenges/{id}
  title, creatorUid, exerciseId, targetReps, startAt, endAt, participantUids[]
challenges/{id}/scores/{uid}     // reps, updatedAt
```

**Hợp đồng với Trưởng nhóm** — class `WorkoutSession` nằm trong `data.model`, Trưởng nhóm điền dữ liệu, B lưu. Không đổi tên field khi chưa báo cả nhóm.

**Thứ tự xử lý khi một buổi tập kết thúc** (`saveCompletedSession`):

1. Ghi document mới vào `users/{uid}/sessions`.
2. Trong **một transaction** trên `users/{uid}`: cập nhật streak, XP, tổng rep (luật bên dưới). Dùng transaction để hai buổi lưu sát nhau không đè lên nhau.
3. Xét huy hiệu mới đạt; ghi vào `users/{uid}/badges` nếu chưa có.
4. Tìm thử thách đang diễn ra cùng `exerciseId` mà người dùng tham gia; cộng `correctReps` vào điểm bằng `FieldValue.increment`.
5. Trả về `Resource.success(ProgressResult)` gồm XP vừa nhận, streak mới, danh sách huy hiệu mới để màn Kết quả của Trưởng nhóm hiển thị chúc mừng.

**Luật streak** (theo giờ Việt Nam, `Asia/Ho_Chi_Minh`):

- `lastWorkoutDate` là hôm nay → streak giữ nguyên.
- `lastWorkoutDate` là hôm qua → streak + 1.
- Còn lại (bỏ quá 1 ngày hoặc chưa tập bao giờ) → streak = 1.

**Luật XP**: mỗi buổi được `10 + 2 × correctReps`, thưởng thêm 20 nếu `accuracy ≥ 0.9`. Nếu `weekKey` khác tuần hiện tại thì đặt `weeklyXp = 0` trước khi cộng. Khi đọc bảng xếp hạng, ai có `weekKey` cũ thì coi `weeklyXp` là 0.

**Huy hiệu** (khai báo trong `BadgeRules`, không lưu định nghĩa lên Firestore):

| Mã | Điều kiện |
|---|---|
| `first_workout` | Hoàn thành buổi đầu tiên |
| `streak_3`, `streak_7`, `streak_30` | Streak đạt 3 / 7 / 30 ngày |
| `reps_100`, `reps_1000` | Tổng rep đúng đạt 100 / 1000 |
| `perfect_form` | Một buổi có `accuracy ≥ 0.95` và ít nhất 10 rep |
| `challenge_winner` | Đứng nhất một thử thách đã kết thúc |

**Viết `ProgressService` và `BadgeRules` bằng Java thuần (không dính Android, không dính Firebase)** để viết unit test được. Ví dụ: `int newStreak = ProgressService.computeStreak(lastDate, today, oldStreak);`.

# 5. Chi tiết từng màn hình

**S20 Lịch sử tập**: query `sessions` sắp xếp `startedAt` giảm dần, mỗi lần tải 20 buổi, cuộn tới cuối thì tải tiếp (`limit` + `startAfter`). Gom nhóm theo ngày bằng tiêu đề "Hôm nay / Hôm qua / 12/10". Có trạng thái rỗng kèm nút "Tập buổi đầu tiên".

**S21 Chi tiết buổi tập**: hiện tên bài, thời gian, rep đúng/tổng, độ chính xác dạng vòng tròn %, calo, danh sách lỗi tư thế đã dịch sang tiếng Việt kèm số lần (lấy câu tiếng Việt từ `FormErrorText` do Trưởng nhóm cung cấp). Nút "Tập lại".

**S22 Thống kê**

- Nút chuyển 7 ngày / 30 ngày (`MaterialButtonToggleGroup`).
- Biểu đồ cột: tổng rep đúng mỗi ngày. Biểu đồ đường: độ chính xác trung bình mỗi ngày.
- Ba ô tổng: số buổi, tổng phút tập, calo.
- Chỉ query 1 lần các buổi trong 30 ngày (`startedAt >= mốc`), gom số liệu trong ViewModel, đổi 7/30 không query lại. Ngày không tập vẫn hiện cột 0 để trục thời gian liền mạch.

**S23 Huy hiệu**: lưới tất cả huy hiệu trong `BadgeRules`; đã đạt thì có màu và ngày đạt, chưa đạt thì xám kèm tiến độ (vd. "Streak 4/7").

**S24–S25 Bạn bè**

- Tìm bạn bằng email chính xác (query `emailLower == ...`), không cho liệt kê toàn bộ người dùng.
- Gửi lời mời → tạo `friendRequests`; chặn tự kết bạn với chính mình và gửi trùng.
- Chấp nhận → một `WriteBatch` ghi cả `users/{a}/friends/{b}` và `users/{b}/friends/{a}`, đổi `status = "accepted"`.
- Danh sách bạn hiện avatar, tên, streak; vuốt để hủy kết bạn (hỏi xác nhận).

**S26–S28 Thử thách**

- Tạo: chọn bài tập, mục tiêu rep (10–1000), thời hạn (1–30 ngày), chọn bạn tham gia; người tạo tự có trong `participantUids`.
- Danh sách chia 2 tab: đang diễn ra (`endAt > now`) và đã kết thúc.
- Chi tiết: thanh tiến độ của mình, bảng xếp hạng người tham gia theo `reps` giảm dần (listener realtime), đếm ngược thời gian còn lại. Ai đạt `targetReps` trước có dấu hoàn thành.

**S29 Bảng xếp hạng tuần**: lấy danh sách bạn + bản thân, đọc `users` theo lô tối đa 30 id (`whereIn` trên `FieldPath.documentId()`), sắp theo `weeklyXp` (coi 0 nếu `weekKey` cũ), làm nổi dòng của mình, top 3 có huy chương.

# 6. Code mẫu bắt buộc theo khuôn

Mọi màn hình viết đúng khuôn Fragment → ViewModel → Repository. Trưởng nhóm đã tạo sẵn `Resource`, `BaseFragment`, `FirestorePaths`; B chỉ việc dùng.

**Resource\<T\>** (có sẵn trong `common`):

```java
public class Resource<T> {
    public enum Status { LOADING, SUCCESS, ERROR }
    public final Status status; public final T data; public final String message;
    private Resource(Status s, T d, String m) { status = s; data = d; message = m; }
    public static <T> Resource<T> loading()          { return new Resource<>(Status.LOADING, null, null); }
    public static <T> Resource<T> success(T data)    { return new Resource<>(Status.SUCCESS, data, null); }
    public static <T> Resource<T> error(String msg)  { return new Resource<>(Status.ERROR, null, msg); }
}
```

**Luật nghiệp vụ — Java thuần, test được** (`domain/ProgressService.java`):

```java
public final class ProgressService {
    private ProgressService() {}

    public static int computeStreak(LocalDate lastWorkout, LocalDate today, int oldStreak) {
        if (lastWorkout == null) return 1;
        if (lastWorkout.equals(today)) return Math.max(oldStreak, 1);
        if (lastWorkout.plusDays(1).equals(today)) return oldStreak + 1;
        return 1;
    }

    public static int computeXp(int correctReps, double accuracy) {
        int xp = 10 + 2 * correctReps;
        return accuracy >= 0.9 ? xp + 20 : xp;
    }
}
```

**Unit test** (`test/.../ProgressServiceTest.java`, chạy bằng JUnit, không cần máy ảo):

```java
@Test public void streak_continuesWhenTrainedYesterday() {
    LocalDate today = LocalDate.of(2026, 10, 15);
    assertEquals(5, ProgressService.computeStreak(today.minusDays(1), today, 4));
}
@Test public void streak_resetsAfterMissedDay() {
    LocalDate today = LocalDate.of(2026, 10, 15);
    assertEquals(1, ProgressService.computeStreak(today.minusDays(3), today, 9));
}
```

**Repository dùng transaction** (`SessionRepository.saveCompletedSession`, rút gọn):

```java
DocumentReference userRef = db.collection(FirestorePaths.USERS).document(uid);
db.runTransaction(tx -> {
    DocumentSnapshot u = tx.get(userRef);
    LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    int streak = ProgressService.computeStreak(DateUtils.parse(u.getString("lastWorkoutDate")),
                                               today, intOrZero(u, "streak"));
    int xp = ProgressService.computeXp(session.getCorrectReps(), session.getAccuracy());
    String week = DateUtils.weekKey(today);
    long weekly = week.equals(u.getString("weekKey")) ? longOrZero(u, "weeklyXp") : 0;

    Map<String, Object> upd = new HashMap<>();
    upd.put("streak", streak);
    upd.put("lastWorkoutDate", today.toString());
    upd.put("weekKey", week);
    upd.put("weeklyXp", weekly + xp);
    upd.put("totalXp", FieldValue.increment(xp));
    upd.put("totalReps", FieldValue.increment(session.getCorrectReps()));
    tx.update(userRef, upd);
    return new ProgressResult(xp, streak);
}).addOnSuccessListener(r -> result.setValue(Resource.success(r)))
  .addOnFailureListener(e -> result.setValue(Resource.error(ErrorMapper.toVietnamese(e))));
```

`LocalDate` cần bật **core library desugaring** nếu `minSdk < 26` — báo Trưởng nhóm bật trong `build.gradle`.

**Mọi màn có danh sách phải có đủ 3 trạng thái**: đang tải, rỗng (hình + câu hướng dẫn), lỗi (thông báo + nút Thử lại). Biểu đồ khi chưa có dữ liệu phải hiện chữ "Chưa có buổi tập nào" thay vì khung trống.

# 7. Lịch 12 tuần và tiêu chí hoàn thành

Lịch tính từ thứ Hai 19/10/2026. Mỗi tối Chủ nhật, cập nhật tiến độ vào nhóm chat (đã xong gì, kẹt gì).

**Tuần 1 (19–25/10) — Chuẩn bị**

- ☐ Cài Android Studio bản mới nhất, clone repo, chạy được app khung của Trưởng nhóm
- ☐ Đổi tên frame Figma theo mã S20–S29, xuất icon huy hiệu SVG
- ☐ Thêm MPAndroidChart vào một màn thử, vẽ được biểu đồ với dữ liệu giả

**Tuần 2–3 (26/10–8/11) — Giao diện + luật nghiệp vụ**

- ☐ XML cho S20–S29 khớp Figma, chạy được điều hướng
- ☐ `ProgressService`, `BadgeRules`, `DateUtils.weekKey` + unit test đầy đủ (làm song song vì không cần Firebase)

**Tuần 4–6 (9/11–29/11) — Nối Firebase**

- ☐ `saveCompletedSession` chạy thật với `WorkoutSession` giả
- ☐ Lịch sử phân trang, chi tiết buổi, thống kê 7/30 ngày
- ☐ Bạn bè, lời mời, thử thách, bảng xếp hạng

**Tuần 7–8 (30/11–13/12) — Ghép với nhóm**

- ☐ Màn Kết quả của Trưởng nhóm gọi đúng hàm của B, hiện XP/streak/huy hiệu mới
- ☐ Trang chủ của A thấy streak đổi ngay sau khi tập
- ☐ Gửi Trưởng nhóm danh sách composite index Firestore cần tạo

**Tuần 9–10 (14/12–27/12) — Kiểm thử và hoàn thiện**

- ☐ Thử ranh giới: tập lúc 23:59 rồi 00:01, đổi tuần, bỏ 2 ngày, thử thách hết hạn giữa chừng
- ☐ Thử 2 điện thoại cùng thử thách cập nhật bảng xếp hạng realtime

**Tuần 11–12 (28/12/2026–10/1/2027) — Báo cáo**

- ☐ Viết chương "Module Tiến độ & Cộng đồng": sơ đồ tuần tự lưu buổi tập, bảng luật streak/XP, ảnh màn hình, kết quả unit test
- ☐ Tập trả lời câu hỏi ở mục 8

**Một màn hình chỉ tính là XONG khi**: giống Figma trên 2 cỡ màn; không còn chữ/màu hardcode; có đủ 3 trạng thái tải–rỗng–lỗi; đã qua Pull Request được Trưởng nhóm duyệt; không crash khi xoay màn hoặc mất mạng; phần luật có unit test chạy xanh.

# 8. Lưu ý tránh mất điểm và câu hỏi hội đồng có thể hỏi

**Lỗi hay bị trừ điểm ở phần của B**

- Tính ngày theo giờ UTC → người tập lúc 6 giờ sáng bị tính sang ngày hôm trước, streak sai.
- Cập nhật XP bằng đọc rồi ghi rời rạc (không transaction) → hai buổi lưu sát nhau làm mất điểm.
- Tải toàn bộ lịch sử một lần → chậm và tốn lượt đọc Firestore khi dữ liệu nhiều.
- Biểu đồ bỏ qua ngày không tập → trục thời gian nhảy cóc, đọc sai xu hướng.
- Quên tạo composite index → query thử thách lỗi ngay lúc demo.
- Không có unit test cho phần luật — đây là chỗ dễ lấy điểm nhất mà nhiều nhóm bỏ.

**Câu hỏi hội đồng có thể đặt cho B và hướng trả lời**

| Câu hỏi | Ý chính khi trả lời |
|---|---|
| Người dùng có gian lận điểm bảng xếp hạng được không? | Rep chỉ đến từ AI đếm, Security Rules chỉ cho ghi điểm của chính mình và chỉ được tăng. Thừa nhận giới hạn: sản phẩm thật sẽ kiểm tra lại ở server bằng Cloud Functions. |
| Hai buổi tập lưu cùng lúc thì XP có bị sai không? | Không, cập nhật trong Firestore transaction nên tự thử lại khi có xung đột. |
| Streak tính thế nào khi qua nửa đêm, khi đổi múi giờ? | Theo ngày lịch giờ Việt Nam, có unit test cho các ca ranh giới. |
| 1000 buổi tập thì màn lịch sử có chậm không? | Phân trang 20 buổi/lần; thống kê chỉ query 30 ngày gần nhất. |
| Tại sao không dùng SQLite/Room? | Cần dữ liệu đồng bộ giữa nhiều người (bạn bè, thử thách); Firestore có sẵn cache offline nên vẫn xem được khi mất mạng. |
| Bảng xếp hạng lấy dữ liệu bạn bè thế nào? | Đọc theo lô tối đa 30 id mỗi lần bằng `whereIn`, sắp xếp ở client vì số bạn nhỏ. |

**Điểm cộng nếu còn thời gian**: thông báo khi bạn vượt mình trên bảng xếp hạng, chia sẻ ảnh kết quả buổi tập, xuất lịch sử ra CSV.
