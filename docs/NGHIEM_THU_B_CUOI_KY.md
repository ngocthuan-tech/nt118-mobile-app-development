# Nghiệm thu phần B trên Android

## Kiểm tra trên bản cài

- [x] Hồ sơ: sửa cân nặng rồi đóng và mở lại ứng dụng. Đối chiếu cân nặng với GET users/me. Nhập chiều cao 999 để kiểm tra báo lỗi. Tắt mạng rồi thử tải lại khi có mạng.
- [ ] Trên hai thiết bị dùng cùng tài khoản thử riêng, đổi mật khẩu trên thiết bị thứ nhất. Thử một yêu cầu cần đăng nhập trên thiết bị thứ hai và kiểm tra thông báo hết phiên. Thử đăng nhập bằng mật khẩu mới.
- [ ] Tắt/bật giọng, nghe thử tiếng Việt và mở lại cài đặt. Kiểm tra trên điện thoại có cài dữ liệu giọng Việt. Xác nhận giọng miền Nam bằng nghe thực tế.
- [ ] Tiến độ: kiểm tra một tài khoản trống và một tài khoản có buổi tập. So ngày, tổng rep trên biểu đồ với lịch tháng. Chọn một buổi, xem chi tiết và bấm Tập lại.
- [ ] Huy hiệu: so tổng XP với hạng hồ sơ, thử bộ lọc Đã đạt/Chưa đạt. Kiểm tra mốc chuỗi 7 ngày và mốc 5.000 XP riêng một bài với tài khoản có dữ liệu tương ứng.
- [ ] Thử thách trên máy: tạo hai thử thách, mở từng thẻ và sao chép mã. Mỗi mã có 6 ký tự và hai mã khác nhau. Tìm bạn không dấu, xoay máy, gửi lời mời rồi mở lại. Kiểm tra mã, hạn thử thách và trạng thái Đã mời.
- [ ] Thông báo trên máy: mở một lời mời, kiểm tra trạng thái đã đọc rồi chọn tham gia/từ chối. Kiểm tra nút Trang chủ và Back. Sau khi đăng xuất, Back phải giữ người dùng ngoài màn hồ sơ.

## Phụ thuộc trước khi nghiệm thu với hai tài khoản thật

- [ ] Trưởng nhóm cung cấp API và Retrofit tương ứng cho tạo/liệt kê thử thách, tham gia bằng mã 6 ký tự, mời và trả lời lời mời, tìm bạn, bảng xếp hạng, thông báo và trạng thái đã đọc. Backend quản lý mã duy nhất, thành viên, quyền truy cập, điểm và thời hạn.
- [ ] Khi có API, B nối SocialRepository vào các interface chung. Dùng hai tài khoản trên hai máy để thử cùng một mã, thử lại cùng yêu cầu, kiểm tra danh sách thành viên và điểm sau một buổi tập, rồi đăng nhập lại để đối chiếu dữ liệu.
- [ ] Trưởng nhóm xác nhận API thành tích đã triển khai trên môi trường demo để B kiểm tra huy hiệu từng bài với dữ liệu thật.
- [ ] Trưởng nhóm tích hợp hoặc giao quyền sửa manifest và thành phần lập lịch cho nhắc tập nền. B kiểm tra chọn giờ, đổi giờ, tắt nhắc, mở lại ứng dụng và quyền thông báo trên Android 13 trở lên.
