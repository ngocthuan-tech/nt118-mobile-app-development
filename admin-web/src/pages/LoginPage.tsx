import { useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Alert, Button, Card, Form, Input, Typography } from 'antd';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { useAuth } from '../auth/AuthContext';
import { messageOf } from '../api/client';

interface LoginForm {
  email: string;
  password: string;
}

export default function LoginPage() {
  const { admin, login, notice } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  if (admin) return <Navigate to="/" replace />;
  const from = (location.state as { from?: string } | null)?.from ?? '/';

  const onFinish = async (values: LoginForm) => {
    setLoading(true);
    setError(null);
    try {
      await login(values.email, values.password);
      navigate(from, { replace: true });
    } catch (e) {
      setError(e instanceof Error && !('isAxiosError' in e) ? e.message : messageOf(e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <Card className="login-card" bordered={false}>
        <div className="login-brand">
          <div className="brand-mark">AI</div>
          <div>
            <Typography.Title level={3} style={{ margin: 0 }}>
              AI Coach Admin
            </Typography.Title>
            <Typography.Text type="secondary">Trang quản trị dành cho quản trị viên</Typography.Text>
          </div>
        </div>
        {notice && !error && <Alert type="warning" showIcon message={notice} style={{ marginBottom: 16 }} />}
        {error && <Alert type="error" showIcon message={error} style={{ marginBottom: 16 }} />}
        <Form<LoginForm> layout="vertical" onFinish={onFinish} requiredMark={false} autoComplete="on">
          <Form.Item
            label="Email"
            name="email"
            rules={[
              { required: true, message: 'Nhập email quản trị' },
              { type: 'email', message: 'Email không hợp lệ' },
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="admin@aicoach.vn" autoComplete="username" size="large" />
          </Form.Item>
          <Form.Item label="Mật khẩu" name="password" rules={[{ required: true, message: 'Nhập mật khẩu' }]}>
            <Input.Password prefix={<LockOutlined />} autoComplete="current-password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block size="large" loading={loading}>
            Đăng nhập
          </Button>
        </Form>
        <Typography.Paragraph type="secondary" className="login-note">
          Chỉ tài khoản có vai trò ADMIN mới vào được. Quyền được kiểm tra trên máy chủ cho từng thao tác.
        </Typography.Paragraph>
      </Card>
    </div>
  );
}
