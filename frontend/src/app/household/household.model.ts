export type HouseholdRole = 'admin' | 'member';

export interface HouseholdResponse {
  id: string;
  name: string;
  invitationToken: string;
  createdAt: string;
}

export interface HouseholdSummaryResponse {
  householdId: string;
  householdName: string;
  role: HouseholdRole;
  joined: string;
}

export interface HouseholdMemberResponse {
  memberId: string;
  userName: string;
  role: HouseholdRole;
  experiencePoints: number;
  streakCount: number;
  levelNumber: number;
  levelTitle: string;
  joined: string;
}
