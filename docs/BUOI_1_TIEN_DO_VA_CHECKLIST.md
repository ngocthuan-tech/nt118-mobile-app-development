# Thành viên B — hai buổi hoàn thiện

Ngày: 04/11/2026. Nhánh: `feature/b-progress-social`.

## Chia công việc

- **Buổi 1 — Tiến độ (37–39):** API lịch sử, danh sách/biểu đồ Tuần–Tháng–Năm, lịch tháng và lọc ngày, chi tiết buổi tập, tập lại/chia sẻ, trạng thái tải–lỗi–rỗng, kiểm thử.
- **Buổi 2 — Cài đặt và Cộng đồng:** lưu công tắc/nhắc tập; cài đặt giọng nói và nghe thử; huy hiệu/BadgeRules; danh sách thử thách và bảng xếp hạng; tạo thử thách, mời bạn và thông báo bằng dữ liệu mẫu qua Repository theo yêu cầu giữa kỳ. Chốt bằng kiểm thử toàn luồng B và đối chiếu giao diện.

## Kết quả buổi 1

- `HistoryRepository` chỉ đọc `SessionApi`: danh sách tối đa 50 buổi và chi tiết theo sessionId.
- Màn 37 có danh sách mới nhất trước, số buổi/tổng rep/điểm trung bình và biểu đồ. Tuần = 7 ngày gần nhất; tháng/năm = tháng/năm hiện tại đến hôm nay. Điểm trung bình = trung bình điểm từng buổi trong khoảng chọn.
- Màn 38 có lưới lịch bắt đầu từ thứ Hai, đổi tháng, đánh dấu ngày tập, chọn/bỏ chọn ngày, xem danh sách và thống kê tháng; chuỗi dài nhất tính trên các ngày đã tải trong tháng.
- Màn 39 hiển thị dữ liệu server: rep, đúng/sai, mục tiêu, điểm, thời lượng, kcal, XP, lỗi tư thế. Tập lại dùng exerciseId từ buổi tập; chia sẻ mở trình chọn ứng dụng, không tự gửi.
- Các màn có tải lại/thử lại, lỗi và trạng thái rỗng; giữ dữ liệu cũ khi tải lại lỗi. Chặn yêu cầu tải trùng. Bộ lọc/tháng/ngày chọn được giữ bằng SavedStateHandle.

## Kiểm thử đã thực hiện

- Build debug, 34 unit test (9 test mới) và lint thành công, lint không có lỗi chặn build; vẫn có cảnh báo.
- 3 test Android trên API 37: xử lý lỗi/rỗng/tải lặp, thiếu ID/chi tiết lỗi rồi thử lại, trọn luồng lịch sử → lịch → chi tiết → tập lại đúng bài, tái tạo Activity giữ lựa chọn.
- Xem ảnh giao diện ba màn với dữ liệu kiểm thử. Ảnh cục bộ nằm ở `app/build/reports/history-ui/history-test/` (không commit).
- Được người dùng cho phép đăng nhập demo và **chỉ đọc** API Azure: danh sách trả thành công 16 buổi; GET chi tiết trả đúng ID. Không tạo/sửa/xoá buổi tập. Đây là kiểm tra endpoint thật riêng với kiểm thử UI dùng fixture, không phải kiểm thử toàn luồng UI trên Azure.

Lệnh chạy lại:

```powershell
$env:JAVA_HOME='E:\Android-Dev\JDKs\jdk-21'
$env:ANDROID_HOME='E:\Android-Dev\Sdk'
$env:GRADLE_USER_HOME='E:\Android-Dev\Gradle'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.aicoach.ui.history.HistoryFlowTest'
```

## Checklist người dùng trước khi tạo PR

Mở đúng nhánh B trong Android Studio, chọn máy ảo và Run app. Dùng tài khoản demo của nhóm để kiểm tra dữ liệu có sẵn; không cần sửa hồ sơ/mật khẩu hoặc lưu buổi tập mới.

- [ ] Đăng nhập → bấm **Tiến độ**: tải xong có danh sách hoặc thông báo rỗng đúng khoảng chọn. Không thấy buổi cũ ở Tuần thì thử Năm hoặc lịch tháng trước.
- [ ] Đổi **Tuần / Tháng / Năm**: tiêu đề, biểu đồ và số liệu đổi theo khoảng; tổng rep khớp danh sách; ngày không tập có giá trị 0.
- [ ] Bấm **Tải lại**: thấy trạng thái tải, không tạo danh sách trùng.
- [ ] Mở **Lịch tập** → lùi/tiến tháng: tên tháng và lưới ngày đúng; tháng không có dữ liệu có thông báo rỗng.
- [ ] Chọn ngày có tập: chỉ hiện các buổi của ngày đó. Chọn ngày trống: có thông báo rỗng. Bấm **Xem cả tháng** hoặc bấm lại ngày đang chọn: bỏ lọc.
- [ ] Bấm một buổi trong lịch sử hoặc lịch: chi tiết khớp bài, rep, điểm và thời gian; có đúng/sai, kcal, XP, lỗi nếu có.
- [ ] Bấm **Tập lại bài này**: tới màn chuẩn bị đúng bài; có thể Back mà không bắt đầu/lưu buổi tập.
- [ ] Bấm **Chia sẻ**: mở trình chọn ứng dụng với nội dung buổi tập; huỷ để không gửi cho ai.
- [ ] Xoay màn hình ở Tiến độ/Lịch/Chi tiết: không crash; giữ bộ lọc/tháng/ngày và dữ liệu.
- [ ] Ngắt mạng máy ảo sau khi đã tải → **Tải lại**: báo lỗi, dữ liệu cũ vẫn còn. Bật mạng → **Thử lại**: tải thành công.
- [ ] Với tài khoản riêng chưa có buổi tập: trạng thái rỗng, không có số liệu giả; nút tập buổi đầu mở được màn chuẩn bị.
- [ ] Mở Hồ sơ/Sửa hồ sơ/Đổi mật khẩu và chuyển các tab: không phát sinh lỗi điều hướng từ thay đổi buổi 1.

Nếu có lỗi, ghi màn đang đứng, thao tác, kết quả mong đợi và ảnh lỗi. **Chưa tạo PR cho đến khi người dùng xác nhận hoàn tất checklist.**

## Phạm vi và phối hợp nhánh

- Đã đồng bộ `origin/develop` tại `d76493d` (PR #16 đã merge), kiểm tra lại remote trước khi chốt.
- Thay đổi thuộc module history, HistoryRepository, layout/strings tiến độ và test/tài liệu B. Không sửa navigation, model/API chung, backend hay file Gradle.
- API hiện chỉ cung cấp tối đa N buổi gần nhất, không phân trang. Luôn ghi rõ phạm vi 50 buổi trên màn; không khẳng định đây là toàn bộ lịch sử tháng/năm.
- Server chưa có dữ liệu từng hiệp/góc khớp hoặc lịch tập dự kiến. Không dựng số liệu giả; phần chi tiết hiển thị thông báo dữ liệu chưa có. Những phần này cần hợp đồng API bổ sung nếu yêu cầu ở giai đoạn sau.
