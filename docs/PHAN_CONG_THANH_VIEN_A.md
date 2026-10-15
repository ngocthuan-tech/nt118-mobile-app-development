---
title: "AI Coach — Phân công Thành viên A (Tài khoản & Nội dung)"
date: "15/10/2026"
---

# 1. Vai trò và phạm vi

Thành viên A phụ trách module **Tài khoản & Nội dung**: toàn bộ luồng từ lúc mở app đến lúc người dùng chọn được bài tập, cộng phần hồ sơ và cài đặt. Phần camera AI (đếm rep, sửa tư thế) do Trưởng nhóm làm; phần lịch sử, thống kê, bạn bè, thử thách do Thành viên B làm.

| Mã | Màn hình | Độ khó | Package |
|---|---|---|---|
| S01 | Splash (đã có sẵn, chỉ thêm logic kiểm tra đăng nhập) | Dễ | MainActivity |
| S02 | Onboarding (2–3 slide giới thiệu) | Dễ | ui.auth |
| S03 | Đăng nhập (email + Google) | Trung bình | ui.auth |
| S04 | Đăng ký | Trung bình | ui.auth |
| S05 | Quên mật khẩu | Dễ | ui.auth |
| S06 | Thiết lập hồ sơ lần đầu (chiều cao, cân nặng, mục tiêu, trình độ) | Trung bình | ui.auth |
| S07 | Trang chủ (streak, kế hoạch hôm nay, nút Bắt đầu nhanh) | Khó | ui.home |
| S08 | Thư viện bài tập (9 bài, lọc theo nhóm cơ) | Trung bình | ui.exercise |
| S09 | Chi tiết bài tập (1 layout dùng chung cho cả 9 bài) | Trung bình | ui.exercise |
| S10 | Danh sách kế hoạch tập | Trung bình | ui.plan |
| S11 | Chi tiết kế hoạch (các ngày, các bài trong ngày) | Trung bình | ui.plan |
| S12 | Hồ sơ cá nhân | Dễ | ui.profile |
| S13 | Sửa hồ sơ + đổi ảnh đại diện | Trung bình | ui.profile |
| S14 | Cài đặt (giọng nói bật/tắt, nhắc tập, đơn vị, đăng xuất) | Dễ | ui.profile |

**Điểm giao với người khác** (bắt buộc giữ đúng):

- Nút "Bắt đầu tập" ở S07, S09, S11 chỉ gọi action `action_global_to_readiness` và truyền `exerciseId` (String). Không tự mở camera.
- S07 hiển thị streak và số buổi tuần này bằng cách đọc field `streak`, `weeklyXp` trong `users/{uid}`. Thành viên B là người ghi các field này; A chỉ đọc.
- Cài đặt "Giọng nói" ở S14 lưu vào `SettingsPrefs` (SharedPreferences dùng chung) để module AI đọc.

Nếu tên frame trong Figma khác bảng trên, đổi tên frame Figma theo mã S01–S14 để cả nhóm nói cùng một ngôn ngữ.

# 2. Chuẩn chung toàn nhóm (cả 3 người làm giống hệt nhau)

Cả nhóm code **Java + XML View**, kiến trúc **MVVM**, một Activity duy nhất (`MainActivity`) chứa các Fragment điều hướng bằng **Navigation Component**. Trưởng nhóm dựng sẵn khung project ở Tuần 1; không ai tự tạo Activity mới hay thêm thư viện khi chưa báo nhóm.

**Thư viện thống nhất** (Trưởng nhóm khai báo trong `build.gradle`, đừng tự đổi phiên bản):

- Material Components, ConstraintLayout, Navigation, ViewModel + LiveData, ViewBinding
- Firebase: Auth, Firestore, Storage (BoM)
- Glide (tải ảnh), MPAndroidChart (biểu đồ – Thành viên B dùng)
- CameraX + ML Kit Pose Detection (module AI – Trưởng nhóm dùng)

**Cấu trúc package** (`com.aicoach`):

```
com.aicoach
├── data
│   ├── model        // User, Exercise, Plan, WorkoutSession, Challenge, Badge
│   ├── repository   // UserRepository, ExerciseRepository, ... (1 class / collection)
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

**Luồng dữ liệu bắt buộc**: Fragment → ViewModel → Repository → Firebase. Fragment không bao giờ gọi `FirebaseFirestore.getInstance()` trực tiếp. Mọi kết quả trả về bọc trong `Resource<T>` (LOADING / SUCCESS / ERROR).

**Quy tắc đặt tên**

| Loại | Mẫu | Ví dụ |
|---|---|---|
| Fragment | `<Tên>Fragment` | `LoginFragment`, `ExerciseDetailFragment` |
| ViewModel | `<Tên>ViewModel` | `LoginViewModel` |
| Layout màn hình | `fragment_<tên>.xml` | `fragment_exercise_detail.xml` |
| Layout 1 dòng list | `item_<tên>.xml` | `item_exercise.xml` |
| Icon | `ic_<tên>_24.xml` | `ic_fire_24.xml` |
| Nền/shape | `bg_<tên>.xml` | `bg_card_rounded.xml` |
| String | `<module>_<tên>` | `auth_login_title`, `exercise_start_button` |
| View id | `camelCase` theo vai trò | `btnLogin`, `tvExerciseName`, `rvExercises` |

Không hardcode chữ, màu, kích thước trong XML: chữ vào `strings.xml`, màu vào `colors.xml`, khoảng cách vào `dimens.xml`. Mỗi người chỉ thêm string/màu có tiền tố module của mình để tránh xung đột.

**Điều hướng không đụng nhau**: mỗi module có file graph riêng (`nav_auth.xml`, `nav_main.xml` cho A; `nav_workout.xml` cho Trưởng nhóm; `nav_social.xml` cho B), được `include` vào `nav_root.xml` do Trưởng nhóm giữ.

**Git (GitHub)**

- Nhánh: `main` (bản nộp) ← `develop` ← `feature/<tên-bạn>-<mã-màn>` (vd. `feature/a-s03-login`).
- Không push thẳng vào `main`/`develop`. Mở Pull Request, Trưởng nhóm review rồi merge.
- Commit nhỏ, mô tả rõ: `feat(auth): validate email on login`, `fix(home): streak not refreshing`.
- Pull `develop` mỗi sáng trước khi code. Hội đồng có thể mở lịch sử commit để xem ai làm gì — commit đều đặn là bằng chứng đóng góp.

# 3. Chuyển Figma sang XML trong Android Studio

Figma chỉ là bản vẽ: mình tự dựng lại bằng XML theo số đo trong Figma, không dùng plugin tự sinh code (code sinh ra dùng tọa độ tuyệt đối, vỡ layout trên máy khác và bị hội đồng đánh giá thấp).

1. **Mở Figma ở chế độ Dev Mode / Inspect.** Bấm vào từng phần tử để đọc kích thước, padding, bo góc, màu, font. Frame Figma rộng 360 thì 1 px = 1 dp; nếu frame rộng 390/393 (khung iPhone), vẫn đọc số như dp và kiểm tra lại trên máy thật.
2. **Không tự thêm màu/font.** Trưởng nhóm đã đưa toàn bộ bảng màu vào `colors.xml`, kiểu chữ vào `themes.xml` (`TextAppearance.AICoach.Title`, `.Body`, `.Caption`), khoảng cách vào `dimens.xml` (`space_8`, `space_16`, `radius_card`...). Thiếu thì báo Trưởng nhóm thêm.
3. **Xuất icon dạng SVG** → Android Studio: `File > New > Vector Asset > Local file` → đặt tên `ic_<tên>_24`.
4. **Xuất ảnh (người tập, minh họa) dạng WebP**, khoảng 1080 px chiều dài nhất, đặt trong `res/drawable-nodpi/` với tên `img_<tên>.webp`. Ảnh 9 bài tập do Thành viên A quản lý.
5. **Dựng layout bằng `ConstraintLayout`** cho màn phức tạp, `LinearLayout` cho khối đơn giản. Dùng sẵn component Material (`MaterialButton`, `TextInputLayout`, `MaterialCardView`, `Chip`) để giống Figma mà ít code.
6. **Component lặp lại trong Figma = 1 file XML dùng chung**: thẻ bài tập → `item_exercise.xml`; ô thống kê nhỏ → `view_stat_box.xml` dùng bằng `<include>`.
7. **Danh sách luôn dùng `RecyclerView` + `ListAdapter` + `DiffUtil`**, không dùng `ListView`, không thêm view bằng vòng lặp.
8. **Kiểm tra**: chạy trên 1 máy màn nhỏ (5 inch) và 1 máy lớn, bật Dark mode thử, xoay ngang thử. Chụp màn hình đặt cạnh Figma rồi gửi vào nhóm khi mở Pull Request.

Quy trình mỗi màn: **Tuần dựng UI** chỉ làm XML + dữ liệu giả để giống Figma; **tuần sau** mới nối ViewModel + Firebase. Tách như vậy để không bị kẹt khi Firebase chưa sẵn sàng.

# 4. Chi tiết từng màn hình

**Dữ liệu Firestore A làm việc cùng** (khuôn do Trưởng nhóm chốt, không tự thêm field):

```
users/{uid}
  displayName, email, avatarUrl, heightCm, weightKg,
  goal ("lose_weight" | "build_muscle" | "stay_fit"),
  level ("beginner" | "intermediate" | "advanced"),
  profileCompleted (bool), createdAt, activePlanId, planStartDate
  streak, weeklyXp, totalXp, lastWorkoutDate   // B ghi, A chỉ đọc

exercises/{exerciseId}        // 9 bài, Trưởng nhóm nạp sẵn
  name, muscleGroup, difficulty, imageName, description,
  steps[], tips[], commonMistakes[], defaultReps, caloriesPerRep

plans/{planId}
  title, level, durationDays, coverImageName,
  days[] { dayIndex, items[] { exerciseId, sets, reps } }
```

**S01 Splash**: đã làm sẵn bằng SplashScreen API chuẩn Android 12+ (theme `Theme.AICoach.Starting`), không cần Fragment riêng. Việc của A: trong `MainActivity`, sau khi có `google-services.json`, kiểm tra đăng nhập — chưa đăng nhập thì `navigate(R.id.action_global_to_auth)`; lần đầu mở app thì vào S02 Onboarding; đã đăng nhập nhưng `profileCompleted = false` thì vào S06. Lưu cờ "đã xem onboarding" trong `SettingsPrefs`.

**S02 Onboarding**: `ViewPager2` + chấm chỉ trang, nút Bỏ qua và Tiếp tục.

**S03 Đăng nhập / S04 Đăng ký / S05 Quên mật khẩu**

- Firebase Auth email/mật khẩu; thêm "Đăng nhập bằng Google" nếu kịp (điểm cộng).
- Kiểm tra dữ liệu trước khi gọi Firebase: email đúng định dạng, mật khẩu tối thiểu 6 ký tự, xác nhận mật khẩu khớp. Lỗi hiện ngay dưới ô nhập (`TextInputLayout.setError`).
- Đổi lỗi Firebase sang tiếng Việt dễ hiểu (sai mật khẩu, email đã tồn tại, không có mạng). Không hiện nguyên văn exception.
- Khóa nút + hiện vòng xoay khi đang gửi để tránh bấm hai lần.
- Đăng ký thành công → tạo document `users/{uid}` với `profileCompleted = false` → sang S06.

**S06 Thiết lập hồ sơ**: nhập chiều cao (100–250 cm), cân nặng (25–250 kg), chọn mục tiêu và trình độ bằng `Chip`. Lưu xong đặt `profileCompleted = true` → S07. Trình độ dùng để gợi ý kế hoạch ở S10.

**S07 Trang chủ**: lời chào + tên, ô streak, số XP tuần, thẻ "Kế hoạch hôm nay" (lấy từ plan đang theo), dải bài tập gợi ý ngang, nút Bắt đầu nhanh. Dùng listener realtime cho `users/{uid}` để streak cập nhật ngay sau khi tập xong. Có trạng thái rỗng khi chưa theo kế hoạch nào.

**S08 Thư viện bài tập**: `RecyclerView` dạng lưới 2 cột, `ChipGroup` lọc theo nhóm cơ, ô tìm theo tên. Lọc ngay trong ViewModel (chỉ 9 bài, không cần query lại Firestore).

**S09 Chi tiết bài tập**: một layout dùng chung, nhận `exerciseId` qua Safe Args. Ảnh người thật, mô tả, các bước, mẹo, lỗi hay gặp, số rep mặc định, nút "Bắt đầu tập" → `action_global_to_readiness`.

**S10–S11 Kế hoạch tập**: danh sách plan lọc theo `level` của người dùng; chi tiết hiện từng ngày và bài trong ngày; nút "Theo kế hoạch này" lưu `activePlanId` và `planStartDate` vào `users/{uid}` (hai field này A sở hữu).

**S12–S13 Hồ sơ**: hiện thông tin và chỉ số BMI tính từ chiều cao/cân nặng. Đổi ảnh đại diện: chọn ảnh bằng Photo Picker, nén còn khoảng 512 px, tải lên `Storage: avatars/{uid}.jpg`, lưu URL vào `avatarUrl`.

**S14 Cài đặt**: công tắc giọng nói, công tắc nhắc tập + giờ nhắc (WorkManager, thông báo hằng ngày), đơn vị kg/lb, đăng xuất (xóa back stack, về S03), xóa tài khoản (hỏi xác nhận).

# 5. Code mẫu bắt buộc theo khuôn

Mọi màn hình viết đúng 4 lớp dưới đây. Trưởng nhóm đã tạo sẵn `Resource`, `BaseFragment`, `FirestorePaths`; A chỉ việc dùng.

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

**Repository** — chỉ nơi này được chạm Firebase:

```java
public class ExerciseRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public LiveData<Resource<List<Exercise>>> getAll() {
        MutableLiveData<Resource<List<Exercise>>> result = new MutableLiveData<>(Resource.loading());
        db.collection(FirestorePaths.EXERCISES).get()
          .addOnSuccessListener(snap -> result.setValue(Resource.success(snap.toObjects(Exercise.class))))
          .addOnFailureListener(e -> result.setValue(Resource.error(ErrorMapper.toVietnamese(e))));
        return result;
    }
}
```

**ViewModel** — giữ trạng thái, không giữ `Context` hay `View`:

```java
public class ExerciseLibraryViewModel extends ViewModel {
    private final ExerciseRepository repo = new ExerciseRepository();
    private final LiveData<Resource<List<Exercise>>> exercises = repo.getAll();
    public LiveData<Resource<List<Exercise>>> getExercises() { return exercises; }
}
```

**Fragment** — chỉ vẽ và nhận thao tác:

```java
public class ExerciseLibraryFragment extends BaseFragment<FragmentExerciseLibraryBinding> {
    @Override protected FragmentExerciseLibraryBinding inflate(LayoutInflater i, ViewGroup c) {
        return FragmentExerciseLibraryBinding.inflate(i, c, false);
    }
    @Override public void onViewCreated(@NonNull View v, Bundle s) {
        ExerciseLibraryViewModel vm = new ViewModelProvider(this).get(ExerciseLibraryViewModel.class);
        ExerciseAdapter adapter = new ExerciseAdapter(ex -> navigateToDetail(ex.getId()));
        binding.rvExercises.setAdapter(adapter);
        vm.getExercises().observe(getViewLifecycleOwner(), res -> {
            showLoading(res.status == Resource.Status.LOADING);
            if (res.status == Resource.Status.SUCCESS) adapter.submitList(res.data);
            if (res.status == Resource.Status.ERROR) showError(res.message);
        });
    }
}
```

**Model** — class Java thường, có constructor rỗng và getter (Firestore cần để `toObjects`). Field id lấy bằng `@DocumentId private String id;`.

**Mọi màn có danh sách phải có đủ 3 trạng thái**: đang tải (shimmer hoặc vòng xoay), rỗng (hình + câu hướng dẫn), lỗi (thông báo + nút Thử lại).

# 6. Lịch 12 tuần và tiêu chí hoàn thành

Lịch tính từ thứ Hai 19/10/2026. Mỗi tối Chủ nhật, cập nhật tiến độ vào nhóm chat (đã xong gì, kẹt gì).

**Tuần 1 (19–25/10) — Chuẩn bị**

- ☐ Cài Android Studio bản mới nhất, clone repo, chạy được app khung của Trưởng nhóm trên máy thật hoặc máy ảo
- ☐ Đổi tên frame Figma theo mã S01–S14, xuất icon SVG và 9 ảnh bài tập WebP
- ☐ Đọc lại mục 2 và mục 5, hỏi ngay nếu chỗ nào chưa rõ

**Tuần 2–3 (26/10–8/11) — Dựng giao diện (dữ liệu giả)**

- ☐ XML cho S01–S14 khớp Figma, chạy được điều hướng giữa các màn
- ☐ `item_exercise.xml`, `item_plan.xml`, `view_stat_box.xml`

**Tuần 4–6 (9/11–29/11) — Nối logic + Firebase**

- ☐ Đăng ký, đăng nhập, quên mật khẩu, thiết lập hồ sơ chạy thật
- ☐ Thư viện + chi tiết bài tập đọc từ Firestore, lọc và tìm kiếm
- ☐ Kế hoạch tập, hồ sơ, đổi avatar, cài đặt, nhắc tập

**Tuần 7–8 (30/11–13/12) — Ghép với nhóm**

- ☐ Nút "Bắt đầu tập" mở đúng màn của Trưởng nhóm với `exerciseId`
- ☐ Trang chủ hiện streak/XP do Thành viên B ghi, cập nhật ngay sau khi tập

**Tuần 9–10 (14/12–27/12) — Kiểm thử và hoàn thiện**

- ☐ Thử mất mạng, nhập sai, bấm liên tục, xoay màn, quay lại giữa chừng
- ☐ Viết unit test cho `ValidationUtils` (email, mật khẩu, chiều cao, cân nặng)

**Tuần 11–12 (28/12/2026–10/1/2027) — Báo cáo**

- ☐ Viết chương "Thiết kế và cài đặt module Tài khoản & Nội dung" (use case, sơ đồ tuần tự đăng nhập, ảnh màn hình)
- ☐ Tập trả lời câu hỏi ở mục 7

**Một màn hình chỉ tính là XONG khi**: giống Figma trên 2 cỡ màn; không còn chữ/màu hardcode; có đủ 3 trạng thái tải–rỗng–lỗi; đã qua Pull Request được Trưởng nhóm duyệt; không crash khi xoay màn hoặc mất mạng.

# 7. Lưu ý tránh mất điểm và câu hỏi hội đồng có thể hỏi

**Lỗi hay bị trừ điểm ở phần của A**

- Gọi Firebase thẳng trong Fragment, hoặc dồn hết code vào một Activity khổng lồ.
- Không kiểm tra dữ liệu nhập, hiện nguyên văn lỗi tiếng Anh của Firebase.
- Màn hình trắng trơn khi đang tải hoặc khi không có dữ liệu.
- Ấn Back ở Trang chủ quay về màn Đăng nhập (phải xóa back stack sau khi đăng nhập).
- Ảnh quá nặng làm app giật, APK phình to.
- Toàn bộ commit đứng tên một người — hội đồng sẽ hỏi "vậy bạn làm phần nào?".

**Câu hỏi hội đồng có thể đặt cho A và hướng trả lời**

| Câu hỏi | Ý chính khi trả lời |
|---|---|
| Mật khẩu người dùng lưu ở đâu, có an toàn không? | Không lưu ở app hay Firestore. Firebase Auth giữ và băm mật khẩu phía server; app chỉ giữ phiên đăng nhập. |
| Người khác có đọc được hồ sơ của tôi không? | Firestore Security Rules chỉ cho `request.auth.uid == uid` sửa document của chính mình. |
| Tại sao dùng MVVM? | Tách giao diện khỏi dữ liệu, ViewModel sống qua xoay màn nên không mất dữ liệu đang nhập, dễ test. |
| Tại sao 9 bài tập lại để trên Firestore? | Cập nhật nội dung không cần phát hành bản mới; Firestore có cache offline nên vẫn xem được khi mất mạng. |
| Mất mạng giữa lúc đăng ký thì sao? | Bắt lỗi, báo người dùng bằng tiếng Việt, giữ nguyên dữ liệu đã nhập, cho thử lại. |
| Làm sao chắc màn hình đúng thiết kế? | Đặt ảnh chụp app cạnh frame Figma, dùng chung bảng màu, kiểu chữ, khoảng cách trong `res/values`. |

**Điểm cộng nếu còn thời gian**: đăng nhập Google, chế độ tối (Dark mode) hoàn chỉnh, hỗ trợ TalkBack (đặt `contentDescription` cho ảnh và nút).
