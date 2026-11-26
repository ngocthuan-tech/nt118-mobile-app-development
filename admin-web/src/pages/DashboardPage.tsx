import { useEffect, useState, type ReactNode } from 'react';
import { Alert, Card, Col, Empty, Progress, Row, Segmented, Skeleton, Space, Statistic, Table, Tooltip, Typography } from 'antd';
import {
  CheckCircleOutlined,
  FireOutlined,
  ThunderboltOutlined,
  TrophyOutlined,
  UserAddOutlined,
  UserOutlined,
} from '@ant-design/icons';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip as ChartTooltip,
  XAxis,
  YAxis,
  Legend,
  Line,
  ComposedChart,
} from 'recharts';
import { getStats } from '../api/admin';
import { messageOf } from '../api/client';
import type { FormErrorCount, Stats } from '../api/types';
import { formErrorLabel } from '../constants/labels';
import { formatNumber, shortDay } from '../utils/format';

const RANGES = [
  { label: '7 ngày', value: 7 },
  { label: '30 ngày', value: 30 },
  { label: '90 ngày', value: 90 },
];

export default function DashboardPage() {
  const [days, setDays] = useState(30);
  const [stats, setStats] = useState<Stats | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let alive = true;
    setLoading(true);
    setError(null);
    getStats(days)
      .then((s) => alive && setStats(s))
      .catch((e) => alive && setError(messageOf(e)))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [days]);

  const daily =
    stats?.sessionsPerDay.map((d, i) => ({
      day: shortDay(d.date),
      sessions: d.count,
      signups: stats.signupsPerDay[i]?.count ?? 0,
    })) ?? [];
  const maxError = Math.max(1, ...(stats?.topFormErrors.map((e) => e.count) ?? [1]));

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <div className="page-head">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Tổng quan
          </Typography.Title>
          <Typography.Text type="secondary">
            Số liệu thật từ database, ngày tính theo giờ Việt Nam
          </Typography.Text>
        </div>
        <Segmented options={RANGES} value={days} onChange={(v) => setDays(Number(v))} />
      </div>

      {error && <Alert type="error" showIcon message={error} />}

      <Row gutter={[16, 16]}>
        <StatCard loading={loading} title="Tổng người dùng" value={stats?.totalUsers} icon={<UserOutlined />} color="#1E5EFF" />
        <StatCard loading={loading} title={`Người dùng mới (${days} ngày)`} value={stats?.newUsers} icon={<UserAddOutlined />} color="#12B76A" />
        <StatCard loading={loading} title="Đang hoạt động" hint="Có ít nhất 1 buổi tập trong khoảng ngày" value={stats?.activeUsers} icon={<FireOutlined />} color="#F79009" />
        <StatCard loading={loading} title={`Buổi tập (${days} ngày)`} value={stats?.sessionsInRange} icon={<ThunderboltOutlined />} color="#7A5AF8" />
      </Row>
      <Row gutter={[16, 16]}>
        <StatCard loading={loading} title="Tổng buổi tập từ trước tới nay" value={stats?.totalSessions} icon={<TrophyOutlined />} color="#0BA5EC" span={8} />
        <StatCard loading={loading} title="Tỉ lệ làm đúng (AI chấm)" hint="Số lần làm đúng tư thế / tổng số lần trong khoảng ngày" value={stats?.correctRate} suffix="%" precision={1} icon={<CheckCircleOutlined />} color="#12B76A" span={8} />
        <StatCard loading={loading} title="Thử thách còn hạn" value={stats?.activeChallenges} icon={<TrophyOutlined />} color="#EE46BC" span={8} />
      </Row>

      <Card title="Buổi tập và người đăng ký theo ngày" bordered={false}>
        {loading ? (
          <Skeleton active />
        ) : daily.length === 0 ? (
          <Empty />
        ) : (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
              <ComposedChart data={daily} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="day" tick={{ fontSize: 12 }} interval="preserveStartEnd" minTickGap={16} />
                <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                <ChartTooltip />
                <Legend />
                <Bar dataKey="sessions" name="Buổi tập" fill="#1E5EFF" radius={[4, 4, 0, 0]} />
                <Line dataKey="signups" name="Đăng ký mới" stroke="#12B76A" strokeWidth={2} dot={false} type="monotone" />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        )}
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card title="Bài tập phổ biến" bordered={false} style={{ height: '100%' }}>
            {loading ? (
              <Skeleton active />
            ) : !stats || stats.topExercises.length === 0 ? (
              <Empty description="Chưa có buổi tập trong khoảng ngày này" />
            ) : (
              <div style={{ width: '100%', height: Math.max(220, stats.topExercises.length * 38) }}>
                <ResponsiveContainer>
                  <BarChart data={stats.topExercises} layout="vertical" margin={{ top: 0, right: 16, left: 8, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                    <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                    <YAxis type="category" dataKey="name" width={96} tick={{ fontSize: 12 }} />
                    <ChartTooltip />
                    <Bar dataKey="sessions" name="Số buổi" fill="#7A5AF8" radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title="Lỗi tư thế AI phát hiện nhiều nhất" bordered={false} style={{ height: '100%' }}>
            <Table<FormErrorCount>
              size="small"
              loading={loading}
              rowKey="key"
              pagination={false}
              dataSource={stats?.topFormErrors ?? []}
              locale={{ emptyText: <Empty description="Chưa ghi nhận lỗi tư thế nào" /> }}
              columns={[
                {
                  title: 'Lỗi',
                  dataIndex: 'key',
                  render: (key: string) => (
                    <Space direction="vertical" size={0}>
                      <Typography.Text>{formErrorLabel(key)}</Typography.Text>
                      <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                        {key}
                      </Typography.Text>
                    </Space>
                  ),
                },
                {
                  title: 'Số lần',
                  dataIndex: 'count',
                  width: '45%',
                  render: (count: number) => (
                    <Space style={{ width: '100%' }}>
                      <Progress percent={Math.round((count / maxError) * 100)} showInfo={false} size="small" style={{ width: 120 }} strokeColor="#F04438" />
                      <Typography.Text strong>{formatNumber(count)}</Typography.Text>
                    </Space>
                  ),
                },
              ]}
            />
          </Card>
        </Col>
      </Row>
    </Space>
  );
}

function StatCard(props: {
  loading: boolean;
  title: string;
  hint?: string;
  value: number | undefined;
  suffix?: string;
  precision?: number;
  icon: ReactNode;
  color: string;
  span?: number;
}) {
  const { loading, title, hint, value, suffix, precision, icon, color, span = 6 } = props;
  return (
    <Col xs={24} sm={12} xl={span}>
      <Card bordered={false} className="stat-card">
        <Skeleton loading={loading} active paragraph={{ rows: 1 }}>
          <div className="stat-row">
            <div>
              <Tooltip title={hint}>
                <Typography.Text type="secondary">{title}</Typography.Text>
              </Tooltip>
              <Statistic value={value ?? 0} suffix={suffix} precision={precision} groupSeparator="." decimalSeparator="," />
            </div>
            <div className="stat-icon" style={{ color, background: `${color}14` }}>
              {icon}
            </div>
          </div>
        </Skeleton>
      </Card>
    </Col>
  );
}
