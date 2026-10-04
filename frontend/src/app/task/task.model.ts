export interface DifficultyResponse {
  id: string;
  name: string;
  baseXp: number;
  weight: number;
}

export interface AssignedTaskResponse {
  assignedTaskId: string;
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

export interface TaskCompletionResponse {
  earnedExperiencePoints: number;
  totalExperiencePoints: number;
  streakCount: number;
  levelNumber: number;
  levelTitle: string;
  leveledUp: boolean;
  completedOnTime: boolean;
}
