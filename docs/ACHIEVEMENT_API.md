# EXP cho danh hiệu

`GET /api/v1/users/me/achievements` yêu cầu Bearer token, chỉ trả dữ liệu của tài khoản đăng nhập.

```json
{"success":true,"data":{"totalXp":105,"exercises":[
  {"exerciseId":"pushup","exerciseName":"Hít đất","imageName":"img_pushup","measureType":"reps","totalXp":70},
  {"exerciseId":"plank","exerciseName":"Plank","imageName":"img_plank","measureType":"seconds","totalXp":35}
]}}
```

Ví dụ rút gọn; response thật có toàn bộ bài tập trong thư viện, theo sortOrder. Bài chưa tập có 0 EXP.

- `totalXp` cấp tài khoản giữ nguyên nguồn `users.total_xp`, dùng cho cảnh giới.
- `exercises[].totalXp` tổng hợp toàn bộ buổi tập đã lưu theo tài khoản và exerciseId, không giới hạn 50/100 buổi, dùng cho rank riêng.
- Công thức hiện có không đổi: EXP = 2 × rep đúng + rep sai = totalReps + correctReps. Plank giữ đơn vị giây như luồng lưu buổi tập hiện có.
- Không cộng EXP khi mở màn, không reset theo tuần, không suy ra rank từ chuỗi ngày, điểm BXH mẫu hoặc dữ liệu trên máy.
- Đọc cùng snapshot PostgreSQL (REPEATABLE_READ); không thêm bảng/migration, không sửa API ghi buổi tập.
- Backend cần triển khai trước để app lấy rank từng bài. App không thay thế bằng tổng từ lịch sử bị giới hạn khi endpoint chưa có.

## Kiểm thử

`mvn -B verify` với PostgreSQL theo `backend/src/test/resources/application-test.yml`.
`AchievementFlowTest`: xác thực, tài khoản mới, 2 bài độc lập, tổng 125 buổi, cô lập tài khoản, đọc lặp không cộng EXP.
