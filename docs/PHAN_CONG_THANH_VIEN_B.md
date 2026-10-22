---
title: "AI Coach — Phân công Thành viên B (Hồ sơ, Tiến độ, Cộng đồng) (bản giữa kỳ)"
date: "22/10/2026"
---

**Phạm vi:** Hồ sơ & Cài đặt, Lịch sử & Biểu đồ, Thử thách, Bạn bè, Huy hiệu, Thông báo
**Nhánh làm việc:** `feature/b-progress-social` → Pull Request vào `develop`
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
| GET | `home` | `HomeApi.getHome` | A — Trang chủ (21) |
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

# PHẦN RIÊNG — THÀNH VIÊN B

## B.1 Bạn giữ điểm gì

- **Yêu cầu 2:** API **User Profile** là của bạn — màn Hồ sơ đọc `GET users/me`, màn Sửa hồ sơ ghi `PUT users/me`. Thầy sẽ sửa thử rồi mở lại app xem dữ liệu còn không.
- **Yêu cầu 4:** 12 màn của bạn phải đủ và bấm chuyển được hết.
- So với bản phân công cũ: bạn **nhận thêm** Hồ sơ & Cài đặt (43, 44, 45, 47) và Thông báo (22) từ A để chia đều việc. Streak, XP giờ **server tự tính** khi lưu buổi tập — bạn không phải viết `ProgressService` nữa.

## B.2 Màn hình của bạn

Thứ tự ưu tiên: ★ làm thật + API trước buổi giữa kỳ · ○ khung sườn + dữ liệu mẫu, bấm chuyển màn được.

| Figma | Màn | Dữ liệu | Bấm vào đâu đi đâu | Ưu tiên |
|---|---|---|---|---|
| 43 | **Hồ sơ & Cài đặt** (mở từ avatar Trang chủ) | **GET `users/me`** | Sửa → 44 · Giọng nói → 45 · Huy hiệu → 46 · Đổi mật khẩu (hộp thoại) · Đăng xuất → 47 | ★ |
| 44 | **Chỉnh sửa hồ sơ** | **PUT `users/me`** (tên, chiều cao, cân nặng, mục tiêu, trình độ, số buổi/tuần) | Lưu → quay lại 43 và 43 tải lại | ★ |
| 47 | Xác nhận đăng xuất | `TokenManager.clear()` | "Đăng xuất" → `action_global_to_auth` · "Huỷ" → đóng | ★ |
| — | Đổi mật khẩu (hộp thoại trong 43) | **PUT `users/me/password`** | Thành công → Snackbar | ○ |
| 45 | Cài đặt giọng nói | `SettingsPrefs` (bật/tắt giọng nói — module AI đọc giá trị này) | Back → 43 | ○ |
| 46 | Bộ sưu tập huy hiệu | Dữ liệu mẫu + `BadgeRules` tính từ `totalReps`, `streak` của `users/me` | Back | ○ |
| 37 | **Lịch sử & Biểu đồ** (tab Tiến độ) | **GET `sessions?limit=50`** → danh sách + biểu đồ cột rep 7 ngày (MPAndroidChart) | Một buổi → 39 · Lịch tháng → 38 | ★ |
| 38 | Lịch tập tháng | Từ danh sách `sessions`: tô ngày có tập | Chọn ngày → lọc danh sách | ○ |
| 39 | Chi tiết buổi tập | **GET `sessions/{id}`** (rep đúng/sai, độ chính xác, lỗi tư thế, kcal, XP) | "Tập lại" → `action_global_to_readiness(exerciseId)` | ○ |
| 40 | Thử thách & Bảng xếp hạng (tab Thử thách) | Dữ liệu mẫu `MockSocialData` | "+" → 41 · "Mời bạn" → 42 | ○ |
| 41 | Tạo thử thách | Form, lưu vào danh sách mẫu trong bộ nhớ | Tạo → về 40 | ○ |
| 42 | Mời bạn bè | Dữ liệu mẫu, ô tìm lọc tại chỗ | "Mời" → nút đổi thành "Đã mời" | ○ |
| 22 | Thông báo (mở từ chuông Trang chủ) | Dữ liệu mẫu | Thông báo thử thách → 40 | ○ |

Đã có sẵn màn tạm cho bạn thay: `HistoryFragment` (tab Tiến độ), `ChallengeListFragment` (tab Thử thách), `ProfileFragment` (43), `NotificationsFragment` (22). Avatar và chuông ở Trang chủ đã nối sẵn tới 43 và 22.

## B.3 File của bạn

```
ui/profile/   ProfileFragment (43), EditProfileFragment (44), VoiceSettingsFragment (45),
              BadgesFragment (46), LogoutDialogFragment (47), ChangePasswordDialogFragment,
              NotificationsFragment (22) + ViewModel tương ứng
ui/history/   HistoryFragment (37), MonthCalendarFragment (38), SessionDetailFragment (39), adapter, ViewModel
ui/challenge/ ChallengeListFragment (40), CreateChallengeFragment (41)
ui/social/    InviteFriendsFragment (42)
data/repository/  UserRepository, HistoryRepository, SocialRepository, BadgeRepository
data/mock/    MockSocialData (thử thách, bảng xếp hạng, bạn bè, thông báo), MockBadgeData
domain/       BadgeRules (+ unit test trong app/src/test — điểm cộng "có thuật toán, có kiểm thử")
res/navigation/  nav_social.xml (37–42), nav_profile.xml (43–47), nav_notifications.xml (22)
res/values/   strings_profile.xml, strings_progress.xml, strings_social.xml
```

Bảng xếp hạng, bạn bè, thử thách, thông báo **chưa có API** ở giữa kỳ — dùng `mock(...)` trong Repository, đúng khuôn để giai đoạn 2 chỉ đổi một dòng.

## B.4 Code mẫu phần Hồ sơ

**UserRepository:**

```java
public class UserRepository extends BaseRepository {
    private final UserApi api;

    public UserRepository(Context context) {
        api = ApiClient.create(context, UserApi.class);
    }

    public LiveData<Resource<User>> getMe() {
        return execute(api.getMe());
    }

    public LiveData<Resource<User>> update(ProfileDtos.UpdateProfileRequest body) {
        return execute(api.updateMe(body));
    }

    public LiveData<Resource<Void>> changePassword(String current, String newPassword) {
        return execute(api.changePassword(new ProfileDtos.ChangePasswordRequest(current, newPassword)));
    }
}
```

**EditProfileViewModel** — kiểm tra dữ liệu nhập rồi chỉ gửi field đã đổi:

```java
public void save(String name, String heightText, String weightText, String goal, String level) {
    Double height = parse(heightText);
    Double weight = parse(weightText);
    if (height == null || height < 100 || height > 250) {
        saveState.setValue(Resource.error(getApplication().getString(R.string.profile_error_height)));
        return;
    }
    if (weight == null || weight < 30 || weight > 250) {
        saveState.setValue(Resource.error(getApplication().getString(R.string.profile_error_weight)));
        return;
    }
    LiveData<Resource<User>> src = repo.update(new ProfileDtos.UpdateProfileRequest()
            .displayName(name.trim()).heightCm(height).weightKg(weight).goal(goal).level(level));
    saveState.addSource(src, r -> {
        saveState.setValue(r);
        if (r.status != Resource.Status.LOADING) saveState.removeSource(src);
    });
}
```

`goal` gửi đúng mã `lose_weight | build_muscle | stay_fit`, `level` gửi `beginner | intermediate | advanced` — trên màn hiện chữ tiếng Việt, khi gửi đổi sang mã.

**Màn 43 hiển thị:** tên, email, chữ cái đầu trong avatar (`user.getInitial()`), chiều cao, cân nặng, **BMI = cân nặng / (chiều cao m)²** kèm nhận xét (gầy / bình thường / thừa cân), streak, tổng rep, tổng XP.

**Đăng xuất (47):**

```java
TokenManager.getInstance(requireContext()).clear();
NavHostFragment.findNavController(this).navigate(R.id.action_global_to_auth);
```

**Lịch sử (37):** `HistoryRepository.getSessions(50)` → `execute(api.getSessions(50))`. Nhóm danh sách theo ngày (`startedAt` là giờ UTC dạng ISO — đổi sang giờ Việt Nam bằng `Instant.parse(s).atZone(ZoneId.of("Asia/Ho_Chi_Minh"))`). Biểu đồ: cộng `totalReps` theo từng ngày trong 7 ngày gần nhất.

## B.5 Những lỗi thầy hay bấm thử

- Sửa hồ sơ → thoát app → mở lại → dữ liệu vẫn còn (vì lưu trên server, không phải trên máy).
- Để trống chiều cao, gõ chữ vào ô cân nặng, nhập 999 → báo lỗi dưới ô, không crash.
- Nút Lưu khoá khi đang gửi; chưa sửa gì thì Lưu bị mờ.
- Tài khoản mới chưa tập buổi nào → Lịch sử, biểu đồ, huy hiệu hiện trạng thái rỗng đẹp, không màn trắng.
- Tắt mạng ở màn Hồ sơ → báo lỗi + nút "Thử lại".
- Đăng xuất xong bấm Back → **không** quay lại được Hồ sơ.
- Tên dài trong bảng xếp hạng → `ellipsize="end"`, `maxLines="1"`.

## B.6 Lịch làm việc

Ngày 1 = ngày Trưởng nhóm báo `feature/lead-backend` đã vào `develop`.

| Ngày | Việc | PR |
|---|---|---|
| 1 | Pull `develop`. Tạo **đủ** fragment + layout khung + `<action>` cho mọi màn ở bảng B.2 trong `nav_social.xml`, `nav_profile.xml`, `nav_notifications.xml`; mọi nút đều chuyển màn. Soạn `MockSocialData` giống thật (tên bài trùng 9 bài, ngày gần đây) | PR "khung màn B" |
| 2–3 | 43 Hồ sơ + 47 Đăng xuất: giao diện theo Figma + `GET users/me` | PR Profile |
| 4–5 | 44 Chỉnh sửa hồ sơ + `PUT users/me`; hộp thoại đổi mật khẩu | PR Edit profile |
| 6–7 | 37 Lịch sử & Biểu đồ + 39 Chi tiết buổi tập (API thật) | PR History |
| 8–9 | 38, 40, 41, 42, 45, 46, 22 theo Figma với dữ liệu mẫu; `BadgeRules` + unit test | PR |
| 10 | Sửa lỗi theo review, bấm thử toàn bộ trên máy thật. Đóng băng code | — |

## B.7 Phần bạn trình bày khi demo (~2 phút, điện thoại dùng 4G)

1. Từ Trang chủ bấm avatar → Hồ sơ hiện đúng thông tin **từ server** (tên, chỉ số, BMI, streak).
2. Sửa cân nặng → Lưu → thoát hẳn app → mở lại → số mới vẫn còn. Mở Swagger gọi `GET users/me` cho thầy thấy database đã đổi.
3. Nhập chiều cao 999 → báo lỗi đúng.
4. Tab Tiến độ → lịch sử và biểu đồ của tài khoản demo → chạm một buổi → chi tiết.
5. Lướt nhanh Thử thách → Tạo thử thách → Mời bạn → Huy hiệu → Thông báo: không nút nào chết. Đăng xuất để kết thúc.

## B.8 Câu hỏi phản biện có thể gặp

| Thầy hỏi | Ý trả lời |
|---|---|
| Sửa hồ sơ sao chỉ gửi vài field? | `PUT users/me` cập nhật từng phần: field nào null thì server giữ nguyên → không ghi đè nhầm dữ liệu, request nhẹ hơn |
| Streak, XP tính ở đâu? | Ở server khi lưu buổi tập, theo giờ Việt Nam — một nguồn sự thật duy nhất, người dùng không sửa được từ app; app chỉ hiển thị |
| Dữ liệu mẫu khác gì gõ cứng vào layout? | Dữ liệu mẫu đi qua Repository, cùng kiểu với API thật → giai đoạn 2 chỉ đổi `mock(...)` thành `execute(api...)`, giao diện giữ nguyên |
| Đăng xuất làm gì? | Xoá token trên máy và xoá back stack về màn Đăng nhập; token cũ tự hết hạn sau 7 ngày (giai đoạn 2 có thể thêm danh sách thu hồi token) |
| Sao chọn MPAndroidChart? | Mã nguồn mở, phổ biến nhất cho Android View/XML, có sẵn biểu đồ cột/đường, chạm xem giá trị, nhẹ |
