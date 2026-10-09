export interface DifficultyResponse {
  id: string;
  name: string;
  baseXp: number;
  weight: number;
}

export interface AssignedTaskResponse {
  assignedTaskId: string;
  taskId: string
  name: string;
  description: string | null;
  difficultyName: string;
  xpReward: number;
  dueDate: string;
  assignedMemberName: string;
}

export interface CreateTaskRequest {
  name: string;
  description: string | null;
  difficultyId: string;
  dueDate: string;
  intervalDays: number;
}

export interface ActivateTaskRequest {
  dueDate: string;
}

export interface TaskCompletionResponse {
  earnedExperiencePoints: number;
  totalExperiencePoints: number;
  streakCount: number;
  levelNumber: number;
  levelTitle: string;
  leveledUp: boolean;
  completedOnTime: boolean;
}

export interface TaskDetailResponse {
  taskId: string;
  name: string;
  description: string | null;
  difficultyName: string;
  xpReward: number;
  intervalDays: number;
  active: boolean;
  assignedTaskId: string | null;
  assignedMemberName: string | null;
  dueDate: string | null;
  assignedToCurrentUser: boolean;
  completedByCurrentUser: number;
}
