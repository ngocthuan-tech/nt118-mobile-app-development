# Thành viên B — bàn giao phần còn lại giữa kỳ

Ngày kiểm tra: 04/11/2026. Nhánh mới: `codex/b-settings-social`, bắt đầu từ `3259c6a` trên `feature/b-progress-social`.
PR #17 của phần Tiến độ vẫn giữ nguyên; chưa mở PR mới để người dùng kiểm thử trước.

## Phạm vi và tiến độ

Đối chiếu bảng B.2 trong `docs/PHAN_CONG_THANH_VIEN_B.md`: 13/13 màn/luồng đã có triển khai theo mức yêu cầu giữa kỳ. Đây là hoàn thành phần code của B, chưa phải xác nhận toàn bộ sản phẩm production hoặc nghiệm thu trên máy thật.

| Màn/luồng | Kết quả hiện tại |
|---|---|
| Hồ sơ 43 | GET users/me; tên, email, chiều cao/cân nặng, BMI và phân loại tham khảo 20+, streak/reps/XP; dải huy hiệu; tải/lỗi/thử lại |
| Sửa hồ sơ 44 | PUT users/me, validation, chặn gửi trùng, giữ bản nháp, tải lại Hồ sơ sau lưu (đã có từ PR trước) |
| Đăng xuất 47 | Xoá token, xoá back stack; thêm xoá dữ liệu cộng đồng mẫu nhập trong phiên |
| Đổi mật khẩu | PUT users/me/password, validation và trạng thái gửi/lỗi/thành công (đã có từ PR trước) |
| Giọng nói 45 | Lưu bật/tắt, tốc độ, âm lượng, 4 loại lời nhắc; chọn/nghe thử giọng tiếng Việt thực tế của thiết bị |
| Huy hiệu 46 | 6 mốc từ totalReps/streak thật; bộ lọc, tiến độ, trạng thái chưa có, tải/lỗi/thử lại |
| Lịch sử 37 | API sessions, danh sách và biểu đồ; sửa quy tắc nhóm ngày về Asia/Ho_Chi_Minh |
| Lịch tháng 38 | Đánh dấu/lọc ngày, đổi tháng, giữ lựa chọn; cùng múi giờ Việt Nam |
| Chi tiết 39 | GET sessions/{id}, thông số/lỗi, tập lại đúng bài, mở trình chia sẻ |
| Thử thách 40 | Dữ liệu mẫu qua Repository/ViewModel, Bạn bè/Toàn quốc, thứ hạng đúng sau đổi bộ lọc |
| Tạo thử thách 41 | Tên 3–50 ký tự, 4 bài tập theo số lần, mục tiêu 10–1000, thời gian 3/7/14/30 ngày, riêng tư; lưu bộ nhớ rồi về danh sách |
| Mời bạn 42 | Tìm tên có/không dấu, chọn nhiều bạn, giữ chọn khi xoay, đánh dấu Đã mời, không mời trùng, sao chép mã mẫu |
| Thông báo 22 | Lọc loại, đọc tất cả, chấp nhận/từ chối mẫu, điều hướng; chỉ mở thông báo không tự từ chối lời mời |

## Các giới hạn phải nói đúng khi demo

- Cộng đồng là **dữ liệu mẫu trong bộ nhớ**, đúng phạm vi giữa kỳ: không gửi lời mời thật, không có tài khoản bạn bè thật, không cộng XP. Dữ liệu tạo mới mất khi tiến trình app bị đóng hoặc đăng xuất. Mã sao chép chỉ là mã minh hoạ, chưa có API nhập mã/QR/deep link.
- Cài đặt giờ nhắc và tự kiểm tra được lưu trên máy. Bộ lập lịch thông báo nền và việc module AI sử dụng các tuỳ chọn chi tiết không nằm trong triển khai B này. Không giới thiệu đây là hệ thống nhắc tập nền hoặc giọng hướng dẫn AI đã tích hợp hoàn chỉnh.
- TTS ưu tiên giọng tiếng Việt không cần mạng, giữ giọng người dùng đã chọn. Đã thử engine Google trên API 37 tạo WAV bằng `vi-VN-language` (171.336 byte). **Chưa xác nhận giọng nữ miền Nam**; không suy đoán giới tính/vùng miền từ tên voice. Người dùng nghe thử trên máy thật để chọn. Giọng cần mạng được ghi rõ.
- Huy hiệu streak dùng chuỗi ngày **hiện tại** từ API, không phải thành tích chuỗi dài nhất trọn đời. Các mốc 100/500/1000 rep và 3/7/30 ngày là quy tắc trình bày phía ứng dụng, không ghi XP lên server.
- Thống kê lịch sử giới hạn tối đa 50 buổi gần nhất vì API hiện tại. Không suy diễn thành thống kê trọn đời; thông tin từng hiệp/góc khớp chưa có từ API.

## Kiểm tra tự động và endpoint

- Build debug + APK kiểm thử: thành công.
- 40 unit test: đạt, gồm mốc huy hiệu, BMI, múi giờ, validation và API contract.
- 18 kiểm thử Android trên API 37: đạt; gồm các kiểm thử hồi quy Hồ sơ/Đổi mật khẩu/Sửa hồ sơ/Lịch sử, luồng cộng đồng/cài đặt/huy hiệu, và tạo âm thanh bằng TTS thật.
- Lint: 0 lỗi, 279 cảnh báo toàn dự án; không tuyên bố hết cảnh báo.
- API Azure: đăng nhập demo được cấp phép, GET hồ sơ, GET 16 buổi tập, GET chi tiết đều thành công. Không ghi/sửa dữ liệu demo. Kiểm thử UI sử dụng fixture; đọc endpoint thật là phép kiểm tra riêng.
- Đã xem ảnh các màn mới, kiểm tra chữ, thứ hạng, trạng thái, mã mời và dữ liệu mẫu. Ảnh/âm thanh/log để trong thư mục build, không đưa token hoặc thông tin đăng nhập vào commit.

Các tệp cục bộ sau khi chạy:

- `app/build/b-completion-build.log`
- `app/build/b-completion-instrumentation.log`
- `app/build/reports/tests/testDebugUnitTest/index.html`
- `app/build/reports/lint-results-debug.html`
- `app/build/reports/b-completion-ui-final/`

## Checklist anh kiểm thử trước khi tạo PR

Dùng bản debug từ nhánh `codex/b-settings-social`. Đăng nhập bằng tài khoản riêng khi kiểm tra thao tác ghi. Tài khoản demo chỉ dùng xem số liệu.

- [ ] **Hồ sơ:** mở từ avatar; đối chiếu tên, email, chỉ số, reps/XP với tài khoản. Tắt mạng → Làm mới → có lỗi và Thử lại; bật mạng → tải lại được.
- [ ] **Sửa hồ sơ:** nhập chiều cao 999/để trống → lỗi; nhập hợp lệ → Lưu; đóng mở app → số mới vẫn còn. Nút Lưu không gửi hai lần.
- [ ] **Đổi mật khẩu trên tài khoản riêng:** thử mật khẩu hiện tại sai; xác nhận mật khẩu mới không khớp; đổi hợp lệ → đăng xuất → đăng nhập mật khẩu mới. Không đổi mật khẩu demo.
- [ ] **Cài đặt:** đổi công tắc giọng nói, tự kiểm tra và giờ nhắc; quay lại, xoay màn hình, mở lại app → giá trị còn. Việc lưu giờ không đồng nghĩa đã có thông báo nền.
- [ ] **Giọng nói:** bật giọng; chọn giọng thực tế và nghe thử; đổi tốc độ/âm lượng, tắt bật 4 tuỳ chọn; quay lại vẫn còn. Nghe để xác nhận giọng phù hợp, app không tự khẳng định nữ miền Nam. Khi ra khỏi màn, âm thanh dừng.
- [ ] **Huy hiệu:** thử Tất cả/Đã đạt/Chưa đạt; tài khoản mới có tiến độ 0 và lọc Đã đạt có thông báo rỗng; lỗi mạng có Thử lại.
- [ ] **Tiến độ:** đổi Tuần/Tháng/Năm; mở lịch, đổi tháng/chọn ngày; mở buổi tập; Tập lại đúng bài; Chia sẻ chỉ mở bảng chọn, không tự gửi. Xoay màn hình vẫn giữ bộ lọc.
- [ ] **Thử thách:** đổi Bạn bè/Toàn quốc → bục và thứ hạng khớp; tạo thử thách, thử tên trống/quá dài, đổi bài/mục tiêu/thời gian; tạo hợp lệ → thấy trong danh sách. Chạm thẻ để chọn các thử thách đã tạo.
- [ ] **Mời bạn:** Mời thêm bạn → tìm không dấu (ví dụ anh dung); tên không tồn tại → trạng thái rỗng; chọn, xoay máy → lựa chọn còn; gửi → Đã mời, không gửi trùng. Sao chép mã phải khớp mã đang hiển thị.
- [ ] **Thông báo:** bộ lọc đúng; Đọc tất cả tắt chấm chưa đọc; mở lời mời chỉ đọc, không tự từ chối; Tham gia/Từ chối đổi trạng thái mẫu, điều hướng đúng.
- [ ] **Đăng xuất:** Huỷ vẫn ở Hồ sơ; xác nhận về Đăng nhập; Back không quay lại Hồ sơ; dữ liệu cộng đồng mẫu đã tạo được xoá.
- [ ] **Máy thật:** font lớn, màn hình nhỏ, mạng 4G và loa hoạt động; không vỡ layout/crash khi Back nhanh hoặc xoay.

## Chạy lại kiểm tra

Trong PowerShell tại thư mục gốc dự án:

```powershell
$env:JAVA_HOME='E:\Android-Dev\JDKs\jdk-21'
$env:ANDROID_HOME='E:\Android-Dev\Sdk'
$env:GRADLE_USER_HOME='E:\Android-Dev\Gradle'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
& 'E:\Android-Dev\Sdk\platform-tools\adb.exe' install -r app/build/outputs/apk/debug/app-debug.apk
& 'E:\Android-Dev\Sdk\platform-tools\adb.exe' install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
& 'E:\Android-Dev\Sdk\platform-tools\adb.exe' shell am instrument -w -e captureScreenshots true com.aicoach.test/androidx.test.runner.AndroidJUnitRunner
```

## Kiểm tra xung đột

Sau lần fetch cuối, mô phỏng `git merge-tree --write-tree` với 11 nhánh remote đều không có xung đột văn bản. Các mốc chính: develop `d76493d`, A `15adb1a`, B/PR #17 `3259c6a`, AI `ff3c865`, backend `ec0a7bf`. Đây là kết quả tại thời điểm kiểm tra; không merge hoặc sửa nhánh của người khác. Báo cáo cục bộ: `app/build/b-merge-checks.json`.

Commit triển khai: `4690201` (cài đặt/huy hiệu/cộng đồng), `bbaecf8` (múi giờ lịch sử).

## Tích hợp và PR

Nhánh được tách khỏi head của PR #17, không tự đẩy thêm commit vào PR đó. Nên merge #17 trước rồi mở PR nhánh mới vào develop; nếu mở trước, phần diff vào develop sẽ còn chứa cả commit Lịch sử của #17.

Thay đổi ngoài các file B: chỉ thêm truy vấn `android.intent.action.TTS_SERVICE` vào AndroidManifest để Android 11+ nhìn thấy bộ giọng đọc. Không nâng AGP/Gradle, không sửa backend, file navigation lõi hoặc code A/AI.

Nguồn cho hai chi tiết nền tảng: [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech), [Android Voice](https://developer.android.com/reference/android/speech/tts/Voice), [CDC — phân loại BMI người lớn](https://www.cdc.gov/bmi/adult-calculator/bmi-categories.html).
