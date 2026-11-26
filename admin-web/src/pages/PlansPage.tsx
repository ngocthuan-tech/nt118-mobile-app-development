import { useCallback, useEffect, useState } from 'react';
import {
  Alert,
  App as AntApp,
  Button,
  Card,
  Collapse,
  Empty,
  Form,
  InputNumber,
  Modal,
  Skeleton,
  Space,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import { EditOutlined, ReloadOutlined, TeamOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getPlans, updatePlanItem } from '../api/admin';
import { messageOf } from '../api/client';
import type { PlanItemRow, PlanRow } from '../api/types';
import { LEVELS, labelOf } from '../constants/labels';
import { volume } from '../utils/format';

interface Editing {
  plan: PlanRow;
  dayIndex: number;
  item: PlanItemRow;
}

export default function PlansPage() {
  const { message } = AntApp.useApp();
  const [plans, setPlans] = useState<PlanRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<Editing | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm<{ sets: number; reps: number }>();

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    getPlans()
      .then(setPlans)
      .catch((e) => setError(messageOf(e)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const openEdit = (plan: PlanRow, dayIndex: number, item: PlanItemRow) => {
    setEditing({ plan, dayIndex, item });
    form.setFieldsValue({ sets: item.sets, reps: item.reps });
  };

  const save = async () => {
    if (!editing) return;
    const { sets, reps } = await form.validateFields();
    setSaving(true);
    try {
      const updated = await updatePlanItem(editing.plan.id, editing.item.id, sets, reps);
      setPlans((list) => list.map((p) => (p.id === updated.id ? updated : p)));
      message.success(`Đã lưu: ${editing.item.exerciseName} ${volume(sets, reps, editing.item.measureType)}`);
      setEditing(null);
    } catch (e) {
      message.error(messageOf(e));
      load(); // giáo án có thể vừa đổi trên server (vd. server khởi động lại) → tải lại cho khớp
    } finally {
      setSaving(false);
    }
  };

  const itemColumns = (plan: PlanRow, dayIndex: number): ColumnsType<PlanItemRow> => [
    { title: 'Bài', dataIndex: 'exerciseName' },
    {
      title: 'Khối lượng',
      key: 'volume',
      width: 160,
      render: (_, i) => volume(i.sets, i.reps, i.measureType),
    },
    {
      title: '',
      key: 'edit',
      width: 90,
      align: 'right',
      render: (_, i) => (
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(plan, dayIndex, i)}>
          Sửa
        </Button>
      ),
    },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <div className="page-head">
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            Giáo án
          </Typography.Title>
          <Typography.Text type="secondary">
            Sửa số hiệp × lần từng bài — người đang theo giáo án thấy số mới ở buổi tập tiếp theo
          </Typography.Text>
        </div>
        <Tooltip title="Tải lại">
          <Button icon={<ReloadOutlined />} onClick={load} />
        </Tooltip>
      </div>
      {error && <Alert type="error" showIcon message={error} />}
      {loading ? (
        <Card bordered={false}>
          <Skeleton active />
        </Card>
      ) : plans.length === 0 ? (
        <Card bordered={false}>
          <Empty />
        </Card>
      ) : (
        <Collapse
          accordion
          defaultActiveKey={plans[0]?.id}
          items={plans.map((plan) => ({
            key: plan.id,
            label: (
              <Space wrap>
                <Typography.Text strong>{plan.title}</Typography.Text>
                <Tag>{labelOf(LEVELS, plan.level)}</Tag>
                {plan.adminEdited && (
                  <Tooltip title="Đã sửa trên web admin — khởi động lại server không ghi đè">
                    <Tag color="blue">Đã chỉnh</Tag>
                  </Tooltip>
                )}
              </Space>
            ),
            extra: (
              <Space size={16}>
                <Typography.Text type="secondary">
                  {plan.days.length} buổi · {plan.sessionsPerWeek} buổi/tuần · ~{plan.minutesPerSession} phút
                </Typography.Text>
                <Tooltip title="Số người đang theo giáo án này">
                  <Typography.Text type="secondary">
                    <TeamOutlined /> {plan.followers}
                  </Typography.Text>
                </Tooltip>
              </Space>
            ),
            children: (
              <div className="plan-days">
                {plan.days.map((day) => (
                  <Card key={day.dayIndex} size="small" title={`Buổi ${day.dayIndex}`} className="plan-day">
                    <Table<PlanItemRow>
                      rowKey="id"
                      size="small"
                      pagination={false}
                      showHeader={false}
                      columns={itemColumns(plan, day.dayIndex)}
                      dataSource={day.items}
                    />
                  </Card>
                ))}
              </div>
            ),
          }))}
        />
      )}

      <Modal
        open={editing !== null}
        title={editing ? `${editing.plan.title} — Buổi ${editing.dayIndex}: ${editing.item.exerciseName}` : ''}
        okText="Lưu"
        cancelText="Huỷ"
        confirmLoading={saving}
        onOk={save}
        onCancel={() => setEditing(null)}
        destroyOnClose
      >
        <Form form={form} layout="inline" requiredMark={false} style={{ rowGap: 12 }}>
          <Form.Item label="Số hiệp" name="sets" rules={[{ required: true, type: 'number', min: 1, max: 10, message: '1–10 hiệp' }]}>
            <InputNumber min={1} max={10} precision={0} />
          </Form.Item>
          <Form.Item
            label={editing?.item.measureType === 'seconds' ? 'Số giây' : 'Số lần'}
            name="reps"
            rules={[{ required: true, type: 'number', min: 1, max: 200, message: '1–200' }]}
          >
            <InputNumber min={1} max={200} precision={0} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
