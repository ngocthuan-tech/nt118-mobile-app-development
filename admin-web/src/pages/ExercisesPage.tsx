import { useCallback, useEffect, useState } from 'react';
import { Alert, App as AntApp, Button, Card, Form, Input, InputNumber, Modal, Space, Table, Tag, Tooltip, Typography } from 'antd';
import { EditOutlined, ReloadOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { getExercises, updateExercise } from '../api/admin';
import { messageOf } from '../api/client';
import type { ExerciseRow, ExerciseUpdate } from '../api/types';
import { DIFFICULTIES, MUSCLE_GROUPS, labelOf } from '../constants/labels';
import { volume } from '../utils/format';

export default function ExercisesPage() {
  const { message } = AntApp.useApp();
  const [rows, setRows] = useState<ExerciseRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<ExerciseRow | null>(null);
  const [saving, setSaving] = useState(false);
  const [form] = Form.useForm<ExerciseUpdate>();

  const load = useCallback(() => {
    setLoading(true);
    setError(null);
    getExercises()
      .then(setRows)
      .catch((e) => setError(messageOf(e)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const openEdit = (row: ExerciseRow) => {
    setEditing(row);
    form.setFieldsValue({
      name: row.name,
      description: row.description ?? '',
      defaultSets: row.defaultSets,
      defaultReps: row.defaultReps,
      caloriesPerRep: row.caloriesPerRep,
    });
  };

  const save = async () => {
    if (!editing) return;
    const values = await form.validateFields();
    setSaving(true);
    try {
      const updated = await updateExercise(editing.id, { ...values, name: values.name.trim() });
      setRows((list) => list.map((r) => (r.id === updated.id ? updated : r)));
      message.success(`Đã lưu "${updated.name}" — app thấy số mới ngay`);
      setEditing(null);
    } catch (e) {
      message.error(messageOf(e));
    } finally {
      setSaving(false);
    }
  };

  const timed = editing?.measureType === 'seconds';
  const columns: ColumnsType<ExerciseRow> = [
    {
      title: 'Bài tập',
      key: 'name',
      render: (_, r) => (
        <Space direction="vertical" size={0}>
          <Space>
            <Typography.Text strong>{r.name}</Typography.Text>
            {r.adminEdited && (
              <Tooltip title="Đã sửa trên web admin — khởi động lại server không ghi đè">
                <Tag color="blue">Đã chỉnh</Tag>
              </Tooltip>
            )}
          </Space>
          <Typography.Text type="secondary" style={{ fontSize: 12 }}>
            {r.id}
          </Typography.Text>
        </Space>
      ),
    },
    { title: 'Nhóm cơ', dataIndex: 'muscleGroup', render: (v: string) => labelOf(MUSCLE_GROUPS, v), width: 130 },
    { title: 'Độ khó', dataIndex: 'difficulty', render: (v: string) => labelOf(DIFFICULTIES, v), width: 110 },
    {
      title: 'Mặc định',
      key: 'volume',
      render: (_, r) => volume(r.defaultSets, r.defaultReps, r.measureType),
      width: 130,
    },
    {
      title: 'Kcal',
      dataIndex: 'caloriesPerRep',
      align: 'right',
      width: 110,
      render: (v: number, r) => `${v} / ${r.measureType === 'seconds' ? 'giây' : 'lần'}`,
    },
    { title: 'Đã tập', dataIndex: 'sessionCount', align: 'right', width: 100, render: (v: number) => `${v} buổi` },
    {
      title: '',
      key: 'edit',
      width: 90,
      align: 'right',
      render: (_, r) => (
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)}>
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
            Bài tập
          </Typography.Title>
          <Typography.Text type="secondary">
            9 bài có AI theo dõi. Sửa ở đây thì màn Thư viện / Chi tiết bài trong app dùng số mới ngay
          </Typography.Text>
        </div>
        <Tooltip title="Tải lại">
          <Button icon={<ReloadOutlined />} onClick={load} />
        </Tooltip>
      </div>
      {error && <Alert type="error" showIcon message={error} />}
      <Card bordered={false} styles={{ body: { padding: 0 } }}>
        <Table<ExerciseRow>
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={rows}
          pagination={false}
          scroll={{ x: 820 }}
          expandable={{
            expandedRowRender: (r) => <Typography.Paragraph style={{ margin: 0 }}>{r.description || '—'}</Typography.Paragraph>,
          }}
        />
      </Card>

      <Modal
        open={editing !== null}
        title={editing ? `Sửa bài: ${editing.name}` : ''}
        okText="Lưu"
        cancelText="Huỷ"
        confirmLoading={saving}
        onOk={save}
        onCancel={() => setEditing(null)}
        destroyOnClose
      >
        <Form<ExerciseUpdate> form={form} layout="vertical" requiredMark={false}>
          <Form.Item
            label="Tên bài"
            name="name"
            rules={[
              { required: true, whitespace: true, message: 'Nhập tên bài' },
              { min: 2, max: 50, message: 'Tên bài cần 2–50 ký tự' },
            ]}
          >
            <Input maxLength={50} showCount />
          </Form.Item>
          <Form.Item label="Mô tả" name="description" rules={[{ max: 1000, message: 'Tối đa 1000 ký tự' }]}>
            <Input.TextArea rows={3} maxLength={1000} showCount />
          </Form.Item>
          <Space size={16} wrap>
            <Form.Item label="Số hiệp mặc định" name="defaultSets" rules={[{ required: true, type: 'number', min: 1, max: 10, message: '1–10 hiệp' }]}>
              <InputNumber min={1} max={10} precision={0} />
            </Form.Item>
            <Form.Item
              label={timed ? 'Số giây mỗi hiệp' : 'Số lần mỗi hiệp'}
              name="defaultReps"
              rules={[{ required: true, type: 'number', min: 1, max: 200, message: '1–200' }]}
            >
              <InputNumber min={1} max={200} precision={0} />
            </Form.Item>
            <Form.Item
              label={timed ? 'Kcal mỗi giây' : 'Kcal mỗi lần'}
              name="caloriesPerRep"
              rules={[{ required: true, type: 'number', min: 0, max: 5, message: '0–5 kcal' }]}
            >
              <InputNumber min={0} max={5} step={0.01} precision={2} />
            </Form.Item>
          </Space>
        </Form>
      </Modal>
    </Space>
  );
}
