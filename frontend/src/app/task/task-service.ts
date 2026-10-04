import { Service, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import {
  AssignedTaskResponse,
  CreateTaskRequest,
  DifficultyResponse,
  TaskCompletionResponse,
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
}
