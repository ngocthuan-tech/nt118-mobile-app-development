# Đổi mật khẩu và kết thúc phiên trên các thiết bị

## Hành vi

- Đổi mật khẩu thành công làm hết hiệu lực **mọi phiên đăng nhập cũ**, kể cả thiết bị vừa đổi mật khẩu. App thông báo thành công rồi yêu cầu đăng nhập lại bằng mật khẩu mới.
- Thiết bị khác nhận thông báo: “Mật khẩu tài khoản đã thay đổi. Thiết bị này đã được đăng xuất để bảo vệ tài khoản. Vui lòng đăng nhập lại bằng mật khẩu mới.”
- App kiểm tra phiên khi chuyển ra trước màn hình và định kỳ khoảng 30 giây sau mỗi lần kiểm tra hoàn tất, chỉ khi Activity đang hiển thị. Yêu cầu API thông thường cũng phát hiện phiên bị thu hồi.
- Không phải thông báo đẩy FCM: app đóng, chạy nền hoặc mất mạng sẽ nhận thông báo khi mở lại/kết nối lại và gọi server. Lỗi mạng và lỗi server 5xx không tự đăng xuất.
- Nhập sai mật khẩu hiện tại, mật khẩu mới không hợp lệ hoặc giống mật khẩu cũ không thu hồi phiên.
- Thông báo giữ được khi xoay màn hình; lý do được lưu để xử lý khi app trở lại. Không dùng nút Back để trở về màn được bảo vệ sau khi đăng xuất.
- Phản hồi trễ của phiên cũ không được xóa token của lần đăng nhập mới.

## Cơ chế và tương thích

- Flyway `V4__session_version.sql` thêm `users.session_version`, mặc định 0. Đổi/đặt lại mật khẩu tăng phiên bản bằng UPDATE nguyên tử trong cùng giao dịch ghi mật khẩu.
- JWT mới chứa `sv` (phiên bản phiên) và `jti` duy nhất. Bộ lọc xác thực so sánh `sv` với dữ liệu server; token đã thu hồi trả 401 kèm `X-Session-Reason: PASSWORD_CHANGED`.
- Token cũ chưa có `sv` chỉ được chấp nhận khi phiên bản tài khoản vẫn bằng 0 và thỏa kiểm tra thời gian đổi mật khẩu cũ. Sau lần đổi đầu tiên, token đó bị từ chối.
- Phiên bản thay cho phép so sánh thời gian đơn thuần: đăng nhập ngay trong cùng giây sau khi đổi mật khẩu vẫn hợp lệ.
- `GET /api/v1/users/me/session` là API cần xác thực, trả envelope thành công với `data: null` nếu phiên hợp lệ.
- Bản ghi hồ sơ dùng cập nhật cột thay đổi; yêu cầu hồ sơ/buổi tập chạy chậm không được ghi đè mật khẩu cũ hoặc giảm phiên bản phiên.
- Android xử lý thành công `data: null` qua `executeCompletion`; không thay đổi quy tắc callback có dữ liệu của luồng đăng nhập/đăng ký.
- Log HTTP debug chỉ giữ mức BASIC, không ghi nội dung mật khẩu/token trả về.

## Điều kiện triển khai

1. Triển khai backend chứa migration V4 và bộ lọc phiên mới trước, chạy Flyway trên database hiện tại. Không chỉ cập nhật APK.
2. Nếu có nhiều instance backend, cập nhật toàn bộ; instance cũ không kiểm tra `sv` sẽ vẫn chấp nhận token đã thu hồi. Tránh để các phiên bản cũ/mới cùng phục vụ sau khi bật tính năng.
3. Cài APK mới lên các thiết bị kiểm thử. APK cũ vẫn bị server từ chối token nhưng có thể chỉ hiện luồng hết phiên chung.
4. Bản thay đổi này chưa được triển khai lên Azure và chưa chạy kiểm thử ghi dữ liệu trên server thật. Cần người có quyền triển khai thực hiện bước 1 trước khi kiểm thử hai thiết bị.

## Checklist kiểm thử hai thiết bị

Sử dụng tài khoản thử riêng; không đổi mật khẩu tài khoản demo dùng chung.

- [ ] Đăng nhập cùng tài khoản trên A và B bằng mật khẩu cũ. Mở thêm tài khoản khác trên C nếu có.
- [ ] Trên A nhập sai mật khẩu hiện tại: thấy lỗi, A và B vẫn dùng được.
- [ ] Trên A đổi mật khẩu hợp lệ: thấy thông báo thành công, được đưa về Đăng nhập; bấm Back không trở lại Hồ sơ/Trang chủ.
- [ ] Giữ B mở và có mạng: trong chu kỳ kiểm tra tiếp theo nhận thông báo đổi mật khẩu và về Đăng nhập. Hoặc gọi API được bảo vệ để phát hiện ngay.
- [ ] Thử B đang chạy nền/đóng/mất mạng: khi mở lại có mạng, B nhận thông báo. Không kỳ vọng thông báo hệ thống khi app đóng.
- [ ] Xoay màn hình khi thông báo hiện: vẫn còn một hộp thoại, không bị lặp.
- [ ] Đăng nhập mật khẩu cũ thất bại; mật khẩu mới thành công ngay trên cả A và B.
- [ ] C vẫn đăng nhập bình thường, không bị ảnh hưởng.
- [ ] Thử quên mật khẩu/đặt lại mật khẩu: các phiên cũ cũng bị thu hồi.

## Bằng chứng kiểm thử ngày 2026-11-09

- Backend CI tại commit `e324ffa`: **20/20** kiểm thử qua với PostgreSQL và Flyway, build Docker qua. Có 6 kiểm thử chuyên biệt: nhiều thiết bị, cùng giây, token cũ, yêu cầu lỗi, tài khoản khác và cập nhật hồ sơ đồng thời.
- Android: **49/49** unit test qua; build APK debug qua; lint không có error, còn 282 warning của toàn dự án.
- Máy ảo API 37: **12/12** kiểm thử mới qua (`SessionSecurityTest`, `PasswordSessionRepositoryTest`, `SessionEndedFlowTest`). Không gọi/ghi dữ liệu Azure trong các kiểm thử này.
- Lần chạy toàn bộ 32 kiểm thử trước khi bổ sung 3 kiểm thử repository: 30 qua, 1 bỏ qua (`livePasswordTest` chưa bật), 1 lỗi ở kiểm thử giọng nói có sẵn `InstalledVoiceTest` với lỗi “TTS engine could not generate speech”. Các kiểm thử phiên mới trong lần chạy đó đều qua. Chưa khẳng định toàn bộ kiểm thử thiết bị đều xanh.

Lệnh kiểm thử tính năng:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.aicoach.data.local.SessionSecurityTest,com.aicoach.data.repository.PasswordSessionRepositoryTest,com.aicoach.ui.profile.SessionEndedFlowTest' :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```
