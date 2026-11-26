# AI Coach — Web Admin

React 18 + Vite + TypeScript + Ant Design 5 + Recharts. Hướng dẫn đầy đủ (chức năng, bảo mật, deploy): [`docs/WEB_ADMIN.md`](../docs/WEB_ADMIN.md).

```bash
npm install
npm run dev      # http://localhost:5173/admin/ — /api chuyển tới server Azure (đổi bằng biến VITE_API_SERVER)
npm test         # unit test (Vitest)
npm run build    # kiểm tra TypeScript + build ra dist/
```

```
src/
├── api/client.ts        axios: baseURL /api/v1, gắn Bearer token, 401/403 → về màn đăng nhập
├── api/admin.ts         hàm gọi API /api/v1/admin/** (kiểu dữ liệu ở api/types.ts)
├── auth/AuthContext.tsx đăng nhập = POST /auth/login rồi GET /admin/me để chắc là ADMIN; token trong sessionStorage
├── layout/AdminLayout.tsx menu trái: Tổng quan · Người dùng · Bài tập · Giáo án
├── pages/               LoginPage, DashboardPage, UsersPage (+ ngăn chi tiết), ExercisesPage, PlansPage
├── constants/labels.ts  mã lỗi AI → tên tiếng Việt, nhãn nhóm cơ / độ khó / mục tiêu
└── utils/format.ts      ngày giờ theo giờ Việt Nam, phần trăm, khối lượng bài
```

Deploy: `backend/docker-compose.yml` có service `admin-web` build thư mục này rồi chép `dist/` vào volume; Caddy phục vụ ở `/admin/`.
