---
title: "AI Coach — Phân công Thành viên A (Tài khoản, Trang chủ, Bài tập, Giáo án) (bản giữa kỳ)"
date: "22/10/2026"
---

**Phạm vi:** Đăng nhập/Đăng ký, khảo sát, Trang chủ, Thư viện + Chi tiết bài tập, Giáo án
**Nhánh làm việc:** `feature/a-auth-content` → Pull Request vào `develop`
**Thay thế** bản phân công ngày 15/10 (bản cũ dùng Firebase — thầy yêu cầu REST API nên đã bỏ).

> File gồm 2 phần. **PHẦN CHUNG** giống hệt 100% trong file của Thành viên A và B — là luật của cả nhóm. **PHẦN RIÊNG** là việc của bạn. Dùng AI hỗ trợ code thì đưa nguyên file này làm ngữ cảnh.

---

# PHẦN CHUNG — giống hệt trong file của A và B (muốn sửa phải báo Trưởng nhóm)

## 0. Thầy chấm giữa kỳ (20% điểm môn) — Backend 4đ · Front-end 6đ

| # | Yêu cầu của thầy | Ai lo | Bằng chứng khi demo |
|---|---|---|---|
| 1 | Back-end deploy lên **VPS Azure**, không localhost, không Vercel (chấm đầu tiên) | Trưởng nhóm — **đã xong** | Swagger chạy trên `aicoach-r15.koreacentral.cloudapp.azure.com`, điện thoại dùng 4G vẫn gọi được |
| 2 | REST API phần di động: **Authentication / User Profile / Trang Home** | A: Authentication + Home · B: User Profile | Đăng ký → đăng nhập → Home có số liệu thật → Hồ sơ → sửa hồ sơ → lưu thành công |
| 3 | Retrofit tạo thành **các abstract service (interface) chứa API**, không gọi API thủ công trong Activity | Trưởng nhóm dựng lõi — **đã xong**; A, B dùng đúng khuôn | Mở code: Fragment → ViewModel → Repository → `XxxApi` (interface) |
| 4 | **Đủ tất cả màn hình**; chưa có chức năng cũng được nhưng **bấm vào phải chuyển màn**, có khung sườn + dữ liệu mẫu. **Thiếu → trừ 3 điểm** | Cả 3 người | Bấm qua toàn bộ app, không nút nào "chết" |

## 1. Hệ thống đã có sẵn (Trưởng nhóm dựng — chỉ dùng, không sửa)

- **Backend:** `https://aicoach-r15.koreacentral.cloudapp.azure.com/api/v1/` — xem và gọi thử mọi API tại **[Swagger](https://aicoach-r15.koreacentral.cloudapp.azure.com/swagger-ui.html)** (bấm *Authorize*, dán `accessToken`).
- **Tài khoản demo:** `demo@aicoach.vn` / `Demo1234` — có sẵn giáo án và 8 buổi tập, dùng để test màn có số liệu.
- **Bỏ Firebase.** App giờ chỉ nói chuyện với backend qua Retrofit. Không ai thêm lại thư viện Firebase.
- **Database:** PostgreSQL trên server. Cấu trúc bảng nằm trong `backend/src/main/resources/db/migration/` (Flyway), sơ đồ quan hệ ở [`docs/DATABASE.md`](DATABASE.md).

| Lớp có sẵn | Nằm ở | Dùng để |
|---|---|---|
| `ApiClient.create(ctx, XxxApi.class)` | `data/remote/` | Lấy service Retrofit (chỉ gọi trong Repository) |
| `AuthApi`, `UserApi`, `HomeApi`, `ExerciseApi`, `PlanApi`, `SessionApi` | `data/remote/api/` | **Các interface chứa API** — đúng yêu cầu số 3 của thầy |
| `AuthInterceptor` | `data/remote/` | Tự gắn token vào mọi request; gặp 401 tự đưa về màn Đăng nhập |
| `BaseRepository.execute(call)` / `execute(call, onSuccess)` / `mock(data)` | `data/repository/` | Gọi API → `LiveData<Resource<T>>`, đã xử lý lỗi mạng và lỗi server |
| `TokenManager` | `data/local/` | `saveToken()`, `isLoggedIn()`, `clear()` |
| `Resource<T>`, `BaseFragment`, `ErrorMapper` | `common/` | Trạng thái LOADING / SUCCESS / ERROR; ViewBinding; câu lỗi tiếng Việt |
| `ExerciseImages.bind(imageView, idHoặcImageName)` | `common/` | Gắn ảnh người tập của 9 bài; chưa có ảnh thì hiện hình người mờ |
| `User`, `HomeData`, `Exercise`, `Plan`, `WorkoutSession`, `Challenge` | `data/model/` | Khớp 1-1 với JSON backend — muốn thêm field thì báo Trưởng nhóm |
| `ProfileDtos.UpdateProfileRequest`, `AuthDtos.*` | `data/remote/dto/` | Body gửi lên server |

## 2. Danh sách API

Mọi response: `{ "success": true|false, "message": "câu tiếng Việt", "data": ... }`. Lỗi đã có `message` tiếng Việt từ server — cứ hiện `res.message`.

| Method | Path | Interface | Ai dùng |
|---|---|---|---|
| POST | `auth/register` `{displayName, email, password}` | `AuthApi.register` | A — Đăng ký (12) |
| POST | `auth/login` `{email, password}` | `AuthApi.login` | A — Đăng nhập (06, 07) |
| GET | `home` → `HomeData` (Chuỗi 7 ngày + Bài tập hôm nay) | `HomeApi.getHome` | A — Trang chủ (21) |
| GET | `exercises?muscleGroup=&difficulty=&q=` · `exercises/{id}` | `ExerciseApi` | A — Thư viện (23), Tìm kiếm (24), Chi tiết (25.x) |
| GET | `plans` · `plans/{id}` | `PlanApi` | A — Giáo án (16, 17, 26) |
| GET | `users/me` | `UserApi.getMe` | B — Hồ sơ (43) |
| PUT | `users/me` (chỉ gửi field cần đổi) | `UserApi.updateMe` | B — Sửa hồ sơ (44) · A — Khảo sát (13–15), chọn giáo án (17, 26) |
| PUT | `users/me/password` | `UserApi.changePassword` | B — Đổi mật khẩu (trong 43) |
| GET | `sessions?limit=` · `sessions/{id}` | `SessionApi` | B — Lịch sử (37, 38), Chi tiết buổi tập (39) |
| POST | `sessions` | `SessionApi.createSession` | Trưởng nhóm — Kết quả buổi tập (35) |

Quy tắc dữ liệu (app kiểm tra trước, server kiểm tra lại): mật khẩu **8–64 ký tự, có chữ và số** · `goal`: `lose_weight | build_muscle | stay_fit` · `level`: `beginner | intermediate | advanced` · chiều cao 100–250 cm · cân nặng 30–250 kg · số buổi/tuần 1–7.

Mã lỗi: `400` dữ liệu sai · `401` sai mật khẩu hoặc hết phiên · `404` không tìm thấy · `409` email đã đăng ký · mất mạng → "Không có kết nối mạng".

## 3. Khuôn code bắt buộc (4 lớp)

```
Fragment (chỉ vẽ + nhận thao tác)
   │ observe LiveData<Resource<T>>
ViewModel (giữ trạng thái, kiểm tra dữ liệu nhập)
   │
Repository extends BaseRepository  ← nơi DUY NHẤT gọi API hoặc trả dữ liệu mẫu
   │
XxxApi (interface Retrofit, có sẵn)
```

**Luật cứng — vi phạm thì Pull Request bị trả lại:**

1. Fragment/ViewModel **không** `import retrofit2`, không gọi `ApiClient`, không `enqueue()`. Kiểm tra: `Ctrl + Shift + F` tìm `retrofit2` → chỉ được nằm trong `data/`.
2. Không gõ URL, không tự thêm header `Authorization`.
3. Không hardcode chữ / màu / kích thước trong XML: chữ vào `strings_<module>.xml` của mình, màu dùng `@color/...` có sẵn, khoảng cách `@dimen/space_...`. Chữ mẫu để xem trước dùng `tools:text`.
4. Dùng ViewBinding qua `BaseFragment`, không `findViewById`.
5. Mỗi màn đủ trạng thái **đang tải / có dữ liệu / lỗi** (và **rỗng** nếu là danh sách). Nút gửi bị khoá khi đang tải.
6. Màn chưa có API: Repository trả `mock(MockXxxData....)` — **cùng kiểu dữ liệu** với API thật, để sau này chỉ đổi 1 dòng.
7. Danh sách dùng `RecyclerView` + `ListAdapter` + `DiffUtil`.

**Mẫu Repository gọi API thật:**

```java
public class ExerciseRepository extends BaseRepository {
    private final ExerciseApi api;

    public ExerciseRepository(Context context) {
        api = ApiClient.create(context, ExerciseApi.class);
    }

    public LiveData<Resource<List<Exercise>>> getExercises(String muscleGroup, String query) {
        return execute(api.getExercises(muscleGroup, null, query));
    }
}
```

**Mẫu Repository dùng dữ liệu mẫu:**

```java
public LiveData<Resource<List<Challenge>>> getChallenges() {
    // TODO(giai-doan-2): return execute(api.getChallenges());
    return mock(MockSocialData.challenges());
}
```

**Mẫu ViewModel** (dùng `AndroidViewModel` vì Repository cần Context):

```java
public class ExerciseLibraryViewModel extends AndroidViewModel {
    private final ExerciseRepository repo;
    private final MediatorLiveData<Resource<List<Exercise>>> exercises = new MediatorLiveData<>();

    public ExerciseLibraryViewModel(@NonNull Application app) {
        super(app);
        repo = new ExerciseRepository(app);
        load(null, null);
    }

    public LiveData<Resource<List<Exercise>>> getExercises() { return exercises; }

    public void load(String muscleGroup, String query) {
        LiveData<Resource<List<Exercise>>> src = repo.getExercises(muscleGroup, query);
        exercises.addSource(src, r -> {
            exercises.setValue(r);
            if (r.status != Resource.Status.LOADING) exercises.removeSource(src);
        });
    }
}
```

Gọi `load()` trong constructor → xoay màn hình **không** gọi lại API.

**Mẫu Fragment:**

```java
public class ExerciseLibraryFragment extends BaseFragment<FragmentExerciseLibraryBinding> {
    @Override
    protected FragmentExerciseLibraryBinding inflate(@NonNull LayoutInflater i, @Nullable ViewGroup c) {
        return FragmentExerciseLibraryBinding.inflate(i, c, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        ExerciseLibraryViewModel vm = new ViewModelProvider(this).get(ExerciseLibraryViewModel.class);
        ExerciseAdapter adapter = new ExerciseAdapter(ex -> openDetail(ex.getId()));
        binding.rvExercises.setAdapter(adapter);

        vm.getExercises().observe(getViewLifecycleOwner(), res -> {
            binding.progress.setVisibility(res.status == Resource.Status.LOADING ? View.VISIBLE : View.GONE);
            if (res.status == Resource.Status.SUCCESS) adapter.submitList(res.data);
            if (res.status == Resource.Status.ERROR) showError(res.message);
        });
    }
}
```

## 4. Điều hướng — mỗi người một file graph, không đụng nhau

| File | Chủ | Ghi chú |
|---|---|---|
| `nav_root.xml` | Trưởng nhóm | Gộp các graph + global action dùng chung |
| `nav_auth.xml`, `nav_main.xml` | A | Đăng nhập, khảo sát · Trang chủ, bài tập, giáo án |
| `nav_social.xml`, `nav_profile.xml`, `nav_notifications.xml` | B | Tiến độ, thử thách · Hồ sơ, cài đặt, huy hiệu · Thông báo |
| `nav_workout.xml` | Trưởng nhóm | Luồng tập AI |

- Hai tab giữ nguyên id: `homeFragment`, `exerciseLibraryFragment` (A) và `historyFragment`, `challengeListFragment` (B) — đổi id là thanh tab hỏng.
- Trong graph của mình: tự thêm `<fragment>` và `<action>`.
- Sang graph của người khác: **chỉ** dùng global action có sẵn trong `nav_root.xml`:

| Global action | Đi tới | Tham số |
|---|---|---|
| `action_global_to_main` | Trang chủ (xoá luồng đăng nhập khỏi back stack) | — |
| `action_global_to_auth` | Luồng đăng nhập (đăng xuất) | — |
| `action_global_to_readiness` | Luồng tập AI | `exerciseId` (String) |
| `action_global_to_profile` | Hồ sơ (B) | — |
| `action_global_to_notifications` | Thông báo (B) | — |

Cần thêm global action mới → nhắn Trưởng nhóm, không tự sửa `nav_root.xml`.

## 5. File của ai người nấy sửa

| Loại | Quy tắc |
|---|---|
| `build.gradle.kts`, `libs.versions.toml`, `AndroidManifest.xml`, `MainActivity`, `nav_root.xml` | Chỉ Trưởng nhóm |
| `common/`, `data/remote/`, `data/model/`, `data/local/TokenManager` | Chỉ Trưởng nhóm — cần field mới thì nhắn |
| `colors.xml`, `themes.xml`, `styles.xml`, `dimens.xml`, `strings.xml`, `strings_common.xml` | Chỉ Trưởng nhóm |
| `strings_<module>.xml` | Mỗi người file riêng: A `strings_auth/home/exercise/plan.xml` · B `strings_profile/progress/social.xml` |
| Layout, drawable | Tên có tiền tố theo module: `fragment_login.xml`, `item_history_session.xml`, `ic_badge_fire_24.xml` |

## 6. Từ Figma sang XML

- Ảnh từng màn ở `docs/figma/` (360 × 800 → 1 px = 1 dp). Mở cạnh Android Studio để dựng XML, đo bằng Figma Dev Mode.
- Màu, kiểu chữ, nút đã có sẵn khớp Figma — xem bảng trong [`docs/MAN_HINH.md`](MAN_HINH.md). Không tự thêm màu mới.
- Dựng bằng `ConstraintLayout` + component Material (`MaterialButton`, `TextInputLayout`, `MaterialCardView`, `Chip`). Không dùng plugin tự sinh code toạ độ tuyệt đối.
- Khối lặp lại = 1 file XML dùng chung (`item_...`, `view_...` + `<include>`).
- Icon: Figma xuất SVG → `File > New > Vector Asset` → `ic_<tên>_24`.

## 7. Git — nhánh và thứ tự đưa vào `main`

```
main      ← bản nộp/demo. Trưởng nhóm merge develop → main trước buổi báo cáo
develop   ← bản tích hợp, luôn build được. Nhận code qua Pull Request
feature/lead-backend        ← Trưởng nhóm (backend + lớp Retrofit) — vào develop đầu tiên
feature/lead-ai-workout     ← Trưởng nhóm (module AI)
feature/a-auth-content      ← Thành viên A
feature/b-progress-social   ← Thành viên B
```

**Bắt đầu (làm 1 lần, sau khi Trưởng nhóm báo `feature/lead-backend` đã vào `develop`):** Android Studio → chuyển sang nhánh của mình → **Git → Pull** → chọn `origin/develop` → Sync Gradle → Run, đăng nhập thử bằng tài khoản demo.

**Mỗi ngày:** Pull `origin/develop` vào nhánh mình → code → Commit (`Ctrl + K`) → Push (`Ctrl + Shift + K`).
Mẫu commit: `feat(auth): gọi API đăng nhập` · `ui(home): dựng thẻ giáo án theo Figma 21` · `fix(profile): lỗi khi chiều cao trống`.

**Pull Request:** GitHub → *New pull request* → base `develop` ← nhánh mình → kèm ảnh chụp màn hình cạnh Figma → chờ **Android CI** xanh → Trưởng nhóm review và merge. Mỗi PR nên gọn (1 nhóm màn), đừng dồn cả tuần vào một PR.

**Conflict** trong file của mình → tự sửa. Trong file của người khác hoặc file chung → không tự sửa, gọi Trưởng nhóm.

**Thứ tự merge cho buổi giữa kỳ:** ① `lead-backend` → ② A: Đăng nhập/Đăng ký → ③ A: Trang chủ → ④ B: Hồ sơ/Sửa hồ sơ → ⑤ các màn khung còn lại của A, B → ⑥ `lead-ai-workout` → **đóng băng code** → Trưởng nhóm merge `develop → main`, gắn tag `v0.5-midterm`.

## 8. Checklist trước khi mở Pull Request

- [ ] `Build > Make Project` không lỗi, chạy được trên máy thật hoặc máy ảo
- [ ] Layout XML thật bám Figma (không dán ảnh chụp Figma làm màn hình)
- [ ] Mọi nút **chuyển được màn** — không dùng Toast "đang phát triển" thay cho điều hướng
- [ ] Dữ liệu đi qua Repository (API thật hoặc `mock(...)`), không gõ cứng dữ liệu trong XML
- [ ] Có đang tải / lỗi / rỗng; tắt mạng không crash; xoay màn hình không mất dữ liệu; bấm Back liên tục không crash
- [ ] Không có `retrofit2` trong `ui/`
- [ ] Chỉ sửa file thuộc quyền mình (mục 5)

---

# PHẦN RIÊNG — THÀNH VIÊN A

## A.1 Bạn giữ điểm gì

- **Yêu cầu 2:** hai trong ba API thầy kiểm tra trực tiếp là của bạn — **Authentication** (Đăng nhập, Đăng ký) và **Trang Home**.
- **Yêu cầu 4:** bạn có nhiều màn nhất (khoảng 20 layout). Thiếu một màn là cả nhóm mất 3 điểm.

## A.2 Màn hình của bạn

Thứ tự ưu tiên: ★ làm thật + API trước buổi giữa kỳ · ○ khung sườn + dữ liệu mẫu, bấm chuyển màn được.

| Figma | Màn | Dữ liệu | Bấm vào đâu đi đâu | Ưu tiên |
|---|---|---|---|---|
| 01 | Splash | Có sẵn (SplashScreen API) | `MainActivity` tự kiểm tra token | — |
| 02–04 | Onboarding 1–3 | Tĩnh, `ViewPager2` | Bỏ qua / Tiếp → 05; lưu `SettingsPrefs.setOnboardingDone(true)` | ○ |
| 05 | Chào mừng | Tĩnh | "Đăng nhập" → 06 · "Tạo tài khoản" → 12 | ○ |
| 06 + 07 | Đăng nhập + trạng thái báo lỗi | **POST `auth/login`** | Thành công: `profileCompleted` = true → `action_global_to_main`, = false → 13 · "Quên mật khẩu" → 08 · "Đăng ký" → 12 | ★ |
| 08–11 | Quên mật khẩu → Nhập mã → Mật khẩu mới → Thành công | Dữ liệu mẫu (mã đúng: `123456`) | 08 → 09 → 10 → 11 → về 06 | ○ |
| 12 | Đăng ký | **POST `auth/register`** | Thành công → 13 | ★ |
| 13–15 | Khảo sát 1/3, 2/3, 3/3 | **PUT `users/me`** (chiều cao, cân nặng, mục tiêu, trình độ, số buổi/tuần) | 13 → 14 → 15 → 16 | ★ |
| 16 | AI đang tạo giáo án | **GET `plans`**, chọn giáo án khớp `level` | Hiệu ứng ~2 giây → 17 | ○ |
| 17 | Giáo án đã sẵn sàng | Giáo án vừa chọn | "Bắt đầu" → **PUT `users/me`** `{activePlanId, profileCompleted: true}` → 20 | ★ |
| 20 | Bật thông báo nhắc tập | `SettingsPrefs` | "Bật" / "Để sau" → `action_global_to_main` | ○ |
| 21 | **Trang chủ** (tab) | **GET `home`** | Xem bảng A.4 | ★ |
| 23 | Thư viện bài tập (tab) | **GET `exercises`** + Chip lọc nhóm cơ | Thẻ bài → 25 · nút lọc → 24 | ★ |
| 24 | Tìm kiếm & bộ lọc | **GET `exercises?q=&muscleGroup=&difficulty=`** (tìm không dấu: "hit dat" ra "Hít đất") | Kết quả → 25 | ○ |
| 25.1–25.9 | Chi tiết bài tập (**1 layout dùng chung cho 9 bài**) | **GET `exercises/{id}`** | "Bắt đầu tập" → `action_global_to_readiness(exerciseId)` | ★ |
| 26 | Chi tiết giáo án | **GET `plans/{id}`** | Bài trong buổi → 25 · "Theo giáo án này" → PUT `activePlanId` · "Tập buổi này" → readiness | ○ |

Màn 22 (Thông báo) và 43–47 (Hồ sơ, Cài đặt, Đăng xuất) đã chuyển sang Thành viên B để chia đều. Chuông và avatar ở Trang chủ đi tới đó bằng `action_global_to_notifications`, `action_global_to_profile` (đã nối sẵn).

## A.3 File của bạn

```
ui/auth/      OnboardingFragment, WelcomeFragment, LoginFragment (đang là màn tạm), RegisterFragment,
              ForgotPassword…Fragment (08–11), Survey…Fragment (13–15), PlanGeneratingFragment (16),
              PlanReadyFragment (17), NotificationPermissionFragment (20) + ViewModel tương ứng
ui/home/      HomeFragment (đã dựng giao diện theo Figma 21 — nối API), HomeViewModel, adapter
ui/exercise/  ExerciseLibraryFragment, ExerciseSearchFragment, ExerciseDetailFragment, adapter, ViewModel
ui/plan/      PlanDetailFragment, ViewModel
data/repository/  AuthRepository, HomeRepository, ExerciseRepository, PlanRepository, OnboardingRepository
data/mock/    MockAuthData (mã xác thực mẫu, nội dung onboarding)
res/navigation/  nav_auth.xml, nav_main.xml
res/values/   strings_auth.xml, strings_home.xml (đã có), strings_exercise.xml, strings_plan.xml
res/drawable-nodpi/  9 ảnh bài tập: img_squat, img_pushup, img_plank, img_lunge, img_glute_bridge,
                     img_dips, img_jumping_jack, img_high_knee, img_toe_touch (WebP, mỗi ảnh < 200 KB)
```

`OnboardingRepository` là của bạn: dùng `UserApi.updateMe` cho khảo sát và chọn giáo án. **Không sửa** `UserRepository` (của B) — hai Repository gọi chung một interface là bình thường.

## A.4 Trang chủ (Figma 21) — Chuỗi 7 ngày + Bài tập hôm nay

Thầy chấm Trang chủ giữa kỳ ở 2 khối: **Chuỗi 7 ngày** và **Bài tập hôm nay**, dữ liệu thật từ database. Cả hai nằm trong **một** API `GET home` → `HomeData`. Giao diện đã dựng sẵn trong `HomeFragment` + `fragment_home.xml`; bạn thay `bindDemoData()` bằng dữ liệu thật. Các khối khác (phút/kcal, thẻ giáo án) có sẵn trong cùng API, làm thêm nếu kịp.

| Trên màn | Lấy từ `HomeData` |
|---|---|
| "Xin chào, Minh" · chữ "M" trong avatar | `getDisplayName()` · chữ cái đầu của tên cuối |
| **"Chuỗi 7 ngày"** | `getStreak()` |
| "Còn 1 buổi để đạt mục tiêu tuần" | `getWeek().getRemainingSessions()` (= 0 → "Đã đạt mục tiêu tuần") |
| **7 chấm T2 → CN** | `getWeek().getDays()`: `getLabel()`, `isActive()` tô màu, `isToday()` viền |
| **"Bài tập hôm nay — 3 bài • ~18 phút"** | `getTodayWorkout().getExercises().size()`, `getTodayWorkout().getEstimatedMinutes()` |
| **Dải thẻ bài tập** | `getTodayWorkout().getExercises()` (`Plan.PlanItem`): `getExerciseName()`, `getSets()` × `getReps()`; plank `isTimeBased()` → "3 × 45 giây"; ảnh `ExerciseImages.bind(img, item.getImageName())` |
| Nút tròn "BẮT ĐẦU TẬP" | `action_global_to_readiness` với `exerciseId` của bài đầu tiên |
| "18 phút hôm nay" · "320 kcal" | `getToday().getMinutes()` · `getToday().getCalories()` |
| Thẻ giáo án | `getActivePlan()` (null = chưa chọn → thẻ "Chọn giáo án" → mở 26) |

```java
public class HomeRepository extends BaseRepository {
    private final HomeApi api;
    public HomeRepository(Context context) { api = ApiClient.create(context, HomeApi.class); }

    public LiveData<Resource<HomeData>> getHome() { return execute(api.getHome()); }
}
```

ViewModel đúng khuôn ở mục 3 (gọi `load()` trong constructor). Thêm `SwipeRefreshLayout` để kéo làm mới.

Đăng nhập `demo@aicoach.vn` để thấy đủ số liệu (streak 5, đang theo giáo án). Tài khoản mới đăng ký: streak 0, 7 chấm trống, 3 bài gợi ý Squat · Hít đất · Plank — **cả hai trạng thái đều phải đẹp**.

## A.5 Code mẫu phần Đăng nhập

**AuthRepository** — lưu token ngay khi thành công:

```java
public class AuthRepository extends BaseRepository {
    private final AuthApi api;
    private final TokenManager tokenManager;

    public AuthRepository(Context context) {
        api = ApiClient.create(context, AuthApi.class);
        tokenManager = TokenManager.getInstance(context);
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> login(String email, String password) {
        return execute(api.login(new AuthDtos.LoginRequest(email, password)),
                res -> tokenManager.saveToken(res.getAccessToken()));
    }

    public LiveData<Resource<AuthDtos.AuthResponse>> register(String name, String email, String password) {
        return execute(api.register(new AuthDtos.RegisterRequest(name, email, password)),
                res -> tokenManager.saveToken(res.getAccessToken()));
    }
}
```

**LoginViewModel** — kiểm tra dữ liệu nhập trước khi gọi mạng:

```java
public void login(String rawEmail, String password) {
    String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        state.setValue(Resource.error(getApplication().getString(R.string.auth_error_email)));
        return;
    }
    if (password == null || password.isEmpty()) {
        state.setValue(Resource.error(getApplication().getString(R.string.auth_error_password_empty)));
        return;
    }
    LiveData<Resource<AuthDtos.AuthResponse>> src = repo.login(email, password);
    state.addSource(src, r -> {
        state.setValue(r);
        if (r.status != Resource.Status.LOADING) state.removeSource(src);
    });
}
```

**LoginFragment** — hiện trạng thái và điều hướng:

```java
vm.getState().observe(getViewLifecycleOwner(), r -> {
    boolean loading = r.status == Resource.Status.LOADING;
    binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    binding.btnLogin.setEnabled(!loading);                 // chống bấm 2 lần
    if (r.status == Resource.Status.ERROR) {
        binding.tilPassword.setError(r.message);           // Figma 07: lỗi đỏ dưới ô nhập
    } else if (r.status == Resource.Status.SUCCESS) {
        NavController nav = NavHostFragment.findNavController(this);
        if (r.data.getUser().isProfileCompleted()) nav.navigate(R.id.action_global_to_main);
        else nav.navigate(R.id.action_login_to_survey1);
    }
});
```

**Khảo sát / chọn giáo án** (`OnboardingRepository`):

```java
public LiveData<Resource<User>> saveBody(double heightCm, double weightKg) {
    return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest()
            .heightCm(heightCm).weightKg(weightKg)));
}

public LiveData<Resource<User>> choosePlan(String planId) {
    return execute(userApi.updateMe(new ProfileDtos.UpdateProfileRequest()
            .activePlanId(planId).profileCompleted(true)));
}
```

## A.6 Những lỗi thầy hay bấm thử

- Email có khoảng trắng hoặc chữ hoa → vẫn đăng nhập được (đã `trim().toLowerCase()`).
- Bấm "Đăng nhập" liên tục → nút khoá khi đang gửi.
- Sai mật khẩu → hiện câu lỗi của server dưới ô mật khẩu (Figma 07), không crash.
- Đăng ký email đã có → lỗi 409 hiện ở ô email.
- Mật khẩu "12345678" (không có chữ) → báo ngay trên app, không cần gửi lên server.
- Tắt mạng → "Không có kết nối mạng" + cho thử lại.
- Đã đăng nhập, tắt app mở lại → vào thẳng Trang chủ. Đăng nhập xong bấm Back → **không** quay lại màn Đăng nhập.
- Xoay màn hình khi đang nhập → không mất chữ đã nhập.
- Ô mật khẩu có nút ẩn/hiện (`app:endIconMode="password_toggle"`), bàn phím bấm "Xong" là gửi luôn.

## A.7 Lịch làm việc

Ngày 1 = ngày Trưởng nhóm báo `feature/lead-backend` đã vào `develop`.

| Ngày | Việc | PR |
|---|---|---|
| 1 | Pull `develop`. Tạo **đủ** fragment + layout khung + `<action>` cho mọi màn ở bảng A.2 trong `nav_auth.xml`, `nav_main.xml`; mọi nút đều chuyển màn. Xuất 9 ảnh bài tập. | PR "khung màn A" |
| 2–3 | 06/07 Đăng nhập, 12 Đăng ký: giao diện theo Figma + API thật | PR Auth |
| 4–5 | 21 Trang chủ nối `GET home` (cả trạng thái có dữ liệu và rỗng) | PR Home |
| 6–7 | 13–17 Khảo sát + chọn giáo án; 23, 25 Thư viện + Chi tiết bài tập | PR |
| 8–9 | Hoàn thiện 02–05, 08–11, 20, 24, 26 theo Figma | PR |
| 10 | Sửa lỗi theo review, bấm thử toàn bộ trên máy thật. Đóng băng code | — |

## A.8 Phần bạn trình bày khi demo (~2 phút, điện thoại dùng 4G)

1. Đăng ký tài khoản mới ngay trước mặt thầy → làm khảo sát → chọn giáo án → vào Trang chủ (trạng thái người mới).
2. Đăng xuất (ở Hồ sơ của B) → đăng nhập lại bằng `demo@aicoach.vn` → Trang chủ đầy đủ streak, giáo án, bài hôm nay **lấy từ server**.
3. Thử nhập sai mật khẩu → báo lỗi đúng Figma 07.
4. Mở code: `LoginFragment` → `LoginViewModel` → `AuthRepository` → `AuthApi` (interface) — Fragment không hề có Retrofit.

## A.9 Câu hỏi phản biện có thể gặp

| Thầy hỏi | Ý trả lời |
|---|---|
| Token lưu ở đâu, có an toàn không? | SharedPreferences chế độ PRIVATE qua `TokenManager`, chỉ app đọc được; chỉ gửi qua HTTPS; hết hạn sau 7 ngày, server trả 401 thì app tự xoá token và về màn Đăng nhập |
| Mật khẩu có bị lộ không? | Đi qua HTTPS; server băm BCrypt, không lưu mật khẩu gốc; log mạng tắt ở bản release và luôn che header Authorization |
| Sao kiểm tra dữ liệu cả ở app lẫn server? | App kiểm tra để phản hồi ngay, đỡ tốn request; server kiểm tra lại vì không bao giờ tin dữ liệu từ client |
| Sao Trang chủ chỉ gọi một API? | `GET home` gộp Chuỗi 7 ngày và Bài tập hôm nay (mô hình Backend-for-Frontend): một lần gọi trên 4G thay vì nhiều lần; server tính sẵn từ database, app chỉ hiển thị |
| Sao không gọi API thẳng trong Fragment? | Tách tầng MVVM: Fragment chỉ hiển thị, Repository lo dữ liệu → đổi dữ liệu mẫu sang API không phải sửa giao diện, ViewModel giữ dữ liệu khi xoay màn |
