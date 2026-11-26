// Kiểu dữ liệu khớp AdminDtos.java (backend) — mọi response bọc trong { success, message, data }.

export interface ApiEnvelope<T> {
  success: boolean;
  message: string | null;
  data: T;
}

export interface AdminMe {
  id: string;
  email: string;
  displayName: string;
  role: 'ADMIN' | 'USER';
}

export interface DayCount {
  date: string; // "2026-11-26" theo giờ Việt Nam
  count: number;
}

export interface ExerciseCount {
  exerciseId: string;
  name: string;
  sessions: number;
}

export interface FormErrorCount {
  key: string;
  count: number;
}

export interface Stats {
  days: number;
  totalUsers: number;
  newUsers: number;
  activeUsers: number;
  totalSessions: number;
  sessionsInRange: number;
  correctRate: number; // %
  activeChallenges: number;
  sessionsPerDay: DayCount[];
  signupsPerDay: DayCount[];
  topExercises: ExerciseCount[];
  topFormErrors: FormErrorCount[];
}

export interface UserRow {
  id: string;
  email: string;
  displayName: string;
  role: 'ADMIN' | 'USER';
  locked: boolean;
  streak: number;
  totalXp: number;
  sessionCount: number;
  createdAt: string;
  lastWorkoutDate: string | null;
}

export interface PageResult<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
}

export interface SessionRow {
  id: string;
  exerciseId: string;
  exerciseName: string;
  startedAt: string;
  durationSec: number;
  totalReps: number;
  correctReps: number;
  accuracy: number; // 0–1
  planId: string | null;
}

export interface UserDetail {
  user: UserRow;
  goal: string | null;
  level: string | null;
  heightCm: number | null;
  weightKg: number | null;
  weeklyGoalSessions: number;
  activePlanId: string | null;
  activePlanTitle: string | null;
  totalReps: number;
  recentSessions: SessionRow[];
}

export interface ExerciseRow {
  id: string;
  name: string;
  muscleGroup: string | null;
  difficulty: string | null;
  measureType: 'reps' | 'seconds' | string;
  description: string | null;
  defaultSets: number;
  defaultReps: number;
  caloriesPerRep: number;
  adminEdited: boolean;
  sessionCount: number;
}

export interface ExerciseUpdate {
  name: string;
  description: string;
  defaultSets: number;
  defaultReps: number;
  caloriesPerRep: number;
}

export interface PlanItemRow {
  id: number;
  exerciseId: string;
  exerciseName: string;
  measureType: string;
  sets: number;
  reps: number;
}

export interface PlanDayRow {
  dayIndex: number;
  items: PlanItemRow[];
}

export interface PlanRow {
  id: string;
  title: string;
  level: string | null;
  durationDays: number;
  sessionsPerWeek: number;
  minutesPerSession: number;
  adminEdited: boolean;
  followers: number;
  days: PlanDayRow[];
}
