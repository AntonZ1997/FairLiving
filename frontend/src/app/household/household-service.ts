import { inject, Service } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {
  HouseholdMemberResponse,
  HouseholdPreviewResponse,
  HouseholdResponse,
  HouseholdSummaryResponse,
} from './household.model';
import { firstValueFrom } from 'rxjs';

const BASE_URL = '/api/households'

@Service()
export class HouseholdService {
  private readonly http = inject(HttpClient);

  async findMine(): Promise<HouseholdSummaryResponse[]> {
    return firstValueFrom(this.http.get<HouseholdSummaryResponse[]>(BASE_URL));
  }

  async findById(householdId: string): Promise<HouseholdResponse> {
    return firstValueFrom(this.http.get<HouseholdResponse>(`${BASE_URL}/${householdId}`));
  }

  async findMembers(householdId: string): Promise<HouseholdMemberResponse[]> {
    return firstValueFrom(
      this.http.get<HouseholdMemberResponse[]>(`${BASE_URL}/${householdId}/members`),
    );
  }

  async create(name: string): Promise<HouseholdResponse> {
    return firstValueFrom(this.http.post<HouseholdResponse>(BASE_URL, { name }));
  }

  async join(invitationId: string): Promise<HouseholdResponse> {
    return firstValueFrom(
      this.http.post<HouseholdResponse>(`${BASE_URL}/join`, { invitationId }),
    );
  }

  async preview(invitationId: string): Promise<HouseholdPreviewResponse> {
    return firstValueFrom(this.http.get<HouseholdPreviewResponse>(`${BASE_URL}/invitations/${encodeURIComponent(invitationId)}`));
  }
}
