import type { ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { App as AntApp, ConfigProvider, Spin } from 'antd';
import viVN from 'antd/locale/vi_VN';
import 'dayjs/locale/vi';
import dayjs from 'dayjs';
import { AuthProvider, useAuth } from './auth/AuthContext';
import AdminLayout from './layout/AdminLayout';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import UsersPage from './pages/UsersPage';
import ExercisesPage from './pages/ExercisesPage';
import PlansPage from './pages/PlansPage';

dayjs.locale('vi');

/** Chưa đăng nhập admin → về /login (giao diện); mọi API vẫn được server kiểm quyền riêng. */
function RequireAdmin({ children }: { children: ReactNode }) {
  const { admin, checking } = useAuth();
  const location = useLocation();
  if (checking) {
    return (
      <div className="center-screen">
        <Spin size="large" />
      </div>
    );
  }
  if (!admin) return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  return <>{children}</>;
}

export default function App() {
  return (
    <ConfigProvider
      locale={viVN}
      theme={{
        token: {
          colorPrimary: '#1E5EFF',
          borderRadius: 10,
          fontFamily:
            "'Be Vietnam Pro', 'Segoe UI', Roboto, -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Arial, sans-serif",
        },
        components: {
          Layout: { siderBg: '#0B1B3F', headerBg: '#ffffff', bodyBg: '#F4F6FB' },
          Menu: { darkItemBg: '#0B1B3F', darkItemSelectedBg: '#1E5EFF' },
        },
      }}
    >
      <AntApp>
        <AuthProvider>
          <BrowserRouter basename="/admin">
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route
                element={
                  <RequireAdmin>
                    <AdminLayout />
                  </RequireAdmin>
                }
              >
                <Route index element={<DashboardPage />} />
                <Route path="users" element={<UsersPage />} />
                <Route path="exercises" element={<ExercisesPage />} />
                <Route path="plans" element={<PlansPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </AuthProvider>
      </AntApp>
    </ConfigProvider>
  );
}
