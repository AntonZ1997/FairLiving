import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import {
  ActivateTaskRequest,
  AssignedTaskResponse,
  CreateTaskRequest,
  DifficultyResponse,
  TaskCompletionResponse,
  TaskDetailResponse,
} from './task.model';

@Service()
export class TaskService {
  private readonly http = inject(HttpClient);

  async findDifficulties(): Promise<DifficultyResponse[]> {
    return firstValueFrom(this.http.get<DifficultyResponse[]>('/api/difficulties'));
  }

  async findOpenTasks(householdId: string): Promise<AssignedTaskResponse[]> {
    return firstValueFrom(
      this.http.get<AssignedTaskResponse[]>(`/api/households/${householdId}/tasks/open`),
    );
  }

  async create(householdId: string, request: CreateTaskRequest): Promise<AssignedTaskResponse> {
    return firstValueFrom(
      this.http.post<AssignedTaskResponse>(`/api/households/${householdId}/tasks`, request),
    );
  }

  async complete(assignedTaskId: string): Promise<TaskCompletionResponse> {
    return firstValueFrom(
      this.http.post<TaskCompletionResponse>(
        `/api/assigned-tasks/${assignedTaskId}/complete`,
        null,
      ),
    );
  }

  async findTaskDetails(taskId: string): Promise<TaskDetailResponse> {
    return firstValueFrom(this.http.get<TaskDetailResponse>(`/api/tasks/${taskId}`));
  }

  async pauseTask(taskId: string): Promise<TaskDetailResponse> {
    return firstValueFrom(this.http.post<TaskDetailResponse>(`/api/tasks/${taskId}/pause`, null)) ;
  }

  async activateTask(taskId: string, request: ActivateTaskRequest): Promise<TaskDetailResponse> {
    return firstValueFrom(this.http.post<TaskDetailResponse>(`/api/tasks/${taskId}/activate`, request));
  }


}
