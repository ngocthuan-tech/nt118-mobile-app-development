import { http, unwrap } from './client';
import type {
  AdminMe,
  ExerciseRow,
  ExerciseUpdate,
  PageResult,
  PlanRow,
  Stats,
  UserDetail,
  UserRow,
} from './types';

interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

/** Đăng nhập bằng API chung của app; quyền ADMIN được xác nhận riêng bằng GET /admin/me. */
export const login = (email: string, password: string) =>
  unwrap<LoginResponse>(http.post('/auth/login', { email, password }));

export const getMe = (token?: string) =>
  unwrap<AdminMe>(http.get('/admin/me', token ? { headers: { Authorization: `Bearer ${token}` } } : undefined));

export const getStats = (days: number) => unwrap<Stats>(http.get('/admin/stats', { params: { days } }));

export const getUsers = (q: string, page: number, size: number) =>
  unwrap<PageResult<UserRow>>(http.get('/admin/users', { params: { q: q || undefined, page, size } }));

export const getUser = (id: string) => unwrap<UserDetail>(http.get(`/admin/users/${encodeURIComponent(id)}`));

export const setLocked = (id: string, locked: boolean) =>
  unwrap<UserRow>(http.put(`/admin/users/${encodeURIComponent(id)}/lock`, { locked }));

export const getExercises = () => unwrap<ExerciseRow[]>(http.get('/admin/exercises'));

export const updateExercise = (id: string, body: ExerciseUpdate) =>
  unwrap<ExerciseRow>(http.put(`/admin/exercises/${encodeURIComponent(id)}`, body));

export const getPlans = () => unwrap<PlanRow[]>(http.get('/admin/plans'));

export const updatePlanItem = (planId: string, itemId: number, sets: number, reps: number) =>
  unwrap<PlanRow>(http.put(`/admin/plans/${encodeURIComponent(planId)}/items/${itemId}`, { sets, reps }));
