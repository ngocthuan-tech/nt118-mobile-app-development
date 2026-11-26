import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  App as AntApp,
  Avatar,
  Button,
  Card,
  Descriptions,
  Drawer,
  Empty,
  Input,
  Popconfirm,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import { LockOutlined, ReloadOutlined, UnlockOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { getUser, getUsers, setLocked } from '../api/admin';
import { messageOf } from '../api/client';
import type { SessionRow, UserDetail, UserRow } from '../api/types';
import { useAuth } from '../auth/AuthContext';
import { GOALS, LEVELS, labelOf } from '../constants/labels';
import { formatDate, formatDateTime, formatDuration, formatNumber, formatPercent01, initial } from '../utils/format';

const PAGE_SIZE = 20;

export default function UsersPage() {
  const { admin } = useAuth();
  const { message } = AntApp.useApp();
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState<{ items: UserRow[]; total: number }>({ items: [], total: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [detailId, setDetailId] = useState<string | null>(null);

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    getUsers(query, page, PAGE_SIZE)
      .then((r) => setData({ items: r.items, total: r.total }))
      .catch((e) => setError(messageOf(e)))
      .finally(() => setLoading(false));
  }, [query, page]);

  useEffect(load, [load]);

  const toggleLock = async (user: UserRow) => {
    setBusyId(user.id);
    try {
      const updated = await setLocked(user.id, !user.locked);
      setData((d) => ({ ...d, items: d.items.map((u) => (u.id === updated.id ? updated : u)) }));
      message.success(updated.locked ? `Đã khoá ${updated.email}` : `Đã mở khoá ${updated.email}`);
    } catch (e) {
      message.error(messageOf(e));
    } finally {
      setBusyId(null);
    }
  };

  const columns: ColumnsType<UserRow> = [
    {
      title: 'Người dùng',
      key: 'user',
      render: (_, u) => (
        <Space>
          <Avatar style={{ background: u.role === 'ADMIN' ? '#7A5AF8' : '#1E5EFF' }}>{initial(u.displayName)}</Avatar>
          <Space direction="vertical" size={0}>
            <Typography.Text strong>{u.displayName}</Typography.Text>
            <Typography.Text type="secondary" style={{ fontSize: 12 }}>
              {u.email}
            </Typography.Text>
          </Space>
        </Space>
      ),
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      width: 110,
      render: (role: string) => (role === 'ADMIN' ? <Tag color="purple">Quản trị</Tag> : <Tag>Người dùng</Tag>),
    },
    {
      title: 'Trạng thái',
      dataIndex: 'locked',
      width: 120,
      render: (locked: boolean) => (locked ? <Tag color="red">Đã khoá</Tag> : <Tag color="green">Hoạt động</Tag>),
    },
    { title: 'Chuỗi', dataIndex: 'streak', width: 100, align: 'right', render: (v: number) => `${v} ngày` },
    { title: 'XP', dataIndex: 'totalXp', width: 90, align: 'right', render: (v: number) => formatNumber(v) },
    { title: 'Buổi tập', dataIndex: 'sessionCount', width: 90, align: 'right' },
    { title: 'Đăng ký', dataIndex: 'createdAt', width: 150, render: formatDateTime, responsive: ['lg'] },
    { title: 'Tập gần nhất', dataIndex: 'lastWorkoutDate', width: 120, render: formatDate, responsive: ['xl'] },
    {
      title: '',
      key: 'action',
      width: 130,
      align: 'right',
      render: (_, u) => {
        const self = u.id === admin?.id;
        const cannot = self || u.role === 'ADMIN';
        if (cannot) {
          return (
            <Tooltip title={self ? 'Không thể tự khoá chính mình' : 'Không khoá tài khoản quản trị'}>
              <Button size="small" icon={<LockOutlined />} disabled>
                Khoá
              </Button>
            </Tooltip>
          );
        }
        return (
          <span onClick={(e) => e.stopPropagation()}>
            <Popconfirm
              title={u.locked ? 'Mở khoá tài khoản?' : 'Khoá tài khoản?'}
              description={
                u.locked
                  ? `${u.email} sẽ đăng nhập lại được.`
                  : `${u.email} bị đăng xuất khỏi mọi thiết bị ngay và không đăng nhập được nữa.`
              }
              okText={u.locked ? 'Mở khoá' : 'Khoá'}
              okButtonProps={{ danger: !u.locked }}
              cancelText="Huỷ"
              onConfirm={() => toggleLock(u)}
            >
              <Button
                size="small"
                danger={!u.locked}
                loading={busyId === u.id}
                icon={u.locked ? <UnlockOutlined /> : <LockOutlined />}
              >
                {u.locked ? 'Mở khoá' : 'Khoá'}
              </Button>
            </Popconfirm>
          </span>
        );
      },
    },
  ];

  const onTableChange = (p: TablePaginationConfig) => setPage((p.current ?? 1) - 1);

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <div className="page-head">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Người dùng
          </Typography.Title>
          <Typography.Text type="secondary">Tìm theo email hoặc tên (gõ không dấu cũng được), bấm một dòng để xem chi tiết</Typography.Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="Tìm email hoặc tên…"
            onSearch={(v) => {
              setPage(0);
              setQuery(v.trim());
            }}
            style={{ width: 280 }}
          />
          <Tooltip title="Tải lại">
            <Button icon={<ReloadOutlined />} onClick={load} />
          </Tooltip>
        </Space>
      </div>
      {error && <Alert type="error" showIcon message={error} />}
      <Card bordered={false} styles={{ body: { padding: 0 } }}>
        <Table<UserRow>
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={data.items}
          scroll={{ x: 900 }}
          onRow={(u) => ({ onClick: () => setDetailId(u.id), style: { cursor: 'pointer' } })}
          onChange={onTableChange}
          pagination={{
            current: page + 1,
            pageSize: PAGE_SIZE,
            total: data.total,
            showSizeChanger: false,
            showTotal: (total) => `${total} tài khoản`,
          }}
          locale={{ emptyText: <Empty description={query ? 'Không tìm thấy tài khoản phù hợp' : 'Chưa có tài khoản'} /> }}
        />
      </Card>
      <UserDetailDrawer userId={detailId} onClose={() => setDetailId(null)} />
    </Space>
  );
}

function UserDetailDrawer({ userId, onClose }: { userId: string | null; onClose: () => void }) {
  const [detail, setDetail] = useState<UserDetail | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userId) return;
    let alive = true;
    setLoading(true);
    setError(null);
    setDetail(null);
    getUser(userId)
      .then((d) => alive && setDetail(d))
      .catch((e) => alive && setError(messageOf(e)))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [userId]);

  const u = detail?.user;
  const sessionColumns: ColumnsType<SessionRow> = [
    { title: 'Thời gian', dataIndex: 'startedAt', render: formatDateTime, width: 140 },
    { title: 'Bài', dataIndex: 'exerciseName' },
    { title: 'Lần đúng / tổng', key: 'reps', render: (_, s) => `${s.correctReps} / ${s.totalReps}`, align: 'right' },
    { title: 'Chính xác', dataIndex: 'accuracy', render: formatPercent01, align: 'right', width: 90 },
    { title: 'Thời lượng', dataIndex: 'durationSec', render: formatDuration, align: 'right', width: 90 },
  ];

  return (
    <Drawer
      open={userId !== null}
      onClose={onClose}
      width={720}
      title={u ? u.displayName : 'Chi tiết người dùng'}
      extra={u && (u.locked ? <Tag color="red">Đã khoá</Tag> : <Tag color="green">Hoạt động</Tag>)}
      destroyOnClose
    >
      {error && <Alert type="error" showIcon message={error} />}
      {loading && <Card loading bordered={false} />}
      {detail && u && (
        <Space direction="vertical" size={20} style={{ width: '100%' }}>
          <Space size={32} wrap>
            <Statistic title="Chuỗi ngày" value={u.streak} suffix="ngày" />
            <Statistic title="XP tích luỹ" value={u.totalXp} groupSeparator="." />
            <Statistic title="Tổng số lần tập" value={detail.totalReps} groupSeparator="." />
            <Statistic title="Buổi tập" value={u.sessionCount} />
          </Space>
          <Descriptions column={{ xs: 1, sm: 2, md: 2, lg: 2, xl: 2, xxl: 2 }} size="small" bordered>
            <Descriptions.Item label="Email">{u.email}</Descriptions.Item>
            <Descriptions.Item label="Vai trò">{u.role === 'ADMIN' ? 'Quản trị' : 'Người dùng'}</Descriptions.Item>
            <Descriptions.Item label="Mục tiêu">{labelOf(GOALS, detail.goal)}</Descriptions.Item>
            <Descriptions.Item label="Trình độ">{labelOf(LEVELS, detail.level)}</Descriptions.Item>
            <Descriptions.Item label="Chiều cao">{detail.heightCm ? `${detail.heightCm} cm` : '—'}</Descriptions.Item>
            <Descriptions.Item label="Cân nặng">{detail.weightKg ? `${detail.weightKg} kg` : '—'}</Descriptions.Item>
            <Descriptions.Item label="Giáo án đang theo">{detail.activePlanTitle ?? '—'}</Descriptions.Item>
            <Descriptions.Item label="Mục tiêu / tuần">{detail.weeklyGoalSessions} buổi</Descriptions.Item>
            <Descriptions.Item label="Đăng ký">{formatDateTime(u.createdAt)}</Descriptions.Item>
            <Descriptions.Item label="Tập gần nhất">{formatDate(u.lastWorkoutDate)}</Descriptions.Item>
          </Descriptions>
          <div>
            <Typography.Title level={5}>10 buổi tập gần nhất</Typography.Title>
            <Table<SessionRow>
              rowKey="id"
              size="small"
              pagination={false}
              columns={sessionColumns}
              dataSource={detail.recentSessions}
              locale={{ emptyText: <Empty description="Chưa có buổi tập nào" /> }}
            />
          </div>
        </Space>
      )}
    </Drawer>
  );
}
