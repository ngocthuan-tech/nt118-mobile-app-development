import { useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Avatar, Button, Dropdown, Grid, Layout, Menu, Typography } from 'antd';
import {
  AppstoreOutlined,
  DashboardOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ScheduleOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { useAuth } from '../auth/AuthContext';
import { initial } from '../utils/format';

const MENU = [
  { key: '/', icon: <DashboardOutlined />, label: 'Tổng quan' },
  { key: '/users', icon: <TeamOutlined />, label: 'Người dùng' },
  { key: '/exercises', icon: <AppstoreOutlined />, label: 'Bài tập' },
  { key: '/plans', icon: <ScheduleOutlined />, label: 'Giáo án' },
];

export default function AdminLayout() {
  const { admin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const screens = Grid.useBreakpoint();
  const [collapsed, setCollapsed] = useState(false);
  const narrow = !screens.lg;

  const selected = MENU.find((m) => m.key !== '/' && location.pathname.startsWith(m.key))?.key ?? '/';

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Layout.Sider
        collapsible
        collapsed={narrow || collapsed}
        trigger={null}
        width={232}
        collapsedWidth={narrow ? 0 : 72}
        className="admin-sider"
      >
        <div className="sider-brand">
          <div className="brand-mark small">AI</div>
          {!(narrow || collapsed) && <span>AI Coach Admin</span>}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[selected]}
          items={MENU}
          onClick={({ key }) => navigate(key)}
        />
      </Layout.Sider>
      <Layout>
        <Layout.Header className="admin-header">
          <Button
            type="text"
            aria-label={collapsed ? 'Mở menu' : 'Thu gọn menu'}
            icon={collapsed || narrow ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed((c) => !c)}
            style={{ visibility: narrow ? 'hidden' : 'visible' }}
          />
          {narrow && (
            <Menu
              mode="horizontal"
              selectedKeys={[selected]}
              items={MENU}
              onClick={({ key }) => navigate(key)}
              style={{ flex: 1, minWidth: 0, borderBottom: 'none' }}
            />
          )}
          <div style={{ flex: narrow ? 0 : 1 }} />
          <Dropdown
            menu={{
              items: [{ key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', onClick: logout }],
            }}
            placement="bottomRight"
          >
            <button type="button" className="admin-user">
              <Avatar style={{ background: '#1E5EFF' }}>{initial(admin?.displayName)}</Avatar>
              {!narrow && (
                <span className="admin-user-text">
                  <Typography.Text strong>{admin?.displayName}</Typography.Text>
                  <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    {admin?.email}
                  </Typography.Text>
                </span>
              )}
            </button>
          </Dropdown>
        </Layout.Header>
        <Layout.Content className="admin-content">
          <Outlet />
        </Layout.Content>
      </Layout>
    </Layout>
  );
}
