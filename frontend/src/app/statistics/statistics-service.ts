import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  CompletionShareResponse,
  CumulatedExperiencePoints,
  HouseholdMemberCompletionCount,
  HouseholdMemberWorkloadShare,
} from './statistics.model';
import { firstValueFrom } from 'rxjs';

@Service()
export class StatisticsService {
  private readonly http = inject(HttpClient);

  async findHouseholdMemberCompletionCounts(
    householdId: string,
    from: string,
    to: string,
  ): Promise<HouseholdMemberCompletionCount[]> {
    return firstValueFrom(
      this.http.get<HouseholdMemberCompletionCount[]>(
        `/api/households/${householdId}/statistics/completed-tasks`,
        { params: { from, to } },
      ),
    );
  }

  async findCompletionShares(
    householdId: string,
    from: string,
    to: string,
  ): Promise<CompletionShareResponse> {
    return firstValueFrom(
      this.http.get<CompletionShareResponse>(
        `/api/households/${householdId}/statistics/completion-shares`,
        { params: { from, to } },
      ),
    );
  }

  async findCumulatedExperiencePoints(
    householdId: string,
    from: string,
    to: string,
  ): Promise<CumulatedExperiencePoints[]> {
    return firstValueFrom(
      this.http.get<CumulatedExperiencePoints[]>(
        `/api/households/${householdId}/statistics/experience-points`,
        { params: { from, to } },
      ),
    );
  }

  async findHouseholdMemberWorkloadShares(
    householdId: string,
    from: string,
    to: string,
  ): Promise<HouseholdMemberWorkloadShare[]> {
    return firstValueFrom(
      this.http.get<HouseholdMemberWorkloadShare[]>(`/api/households/${householdId}/statistics/workload`, {
        params: { from, to },
      }),
    );
  }
}
