export type HouseholdRole = 'admin' | 'member';

export interface HouseholdResponse {
  id: string;
  name: string;
  invitationId: string;
  createdAt: string;
}

export interface HouseholdSummaryResponse {
  householdId: string;
  householdName: string;
  role: HouseholdRole;
  openTaskCount: number;
  memberCount: number;
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
  currentLevelRequiredXp: number;
  nextLevelRequiredXp: number | null;
  isCurrentUser: boolean;
  joined: string;
}

export interface HouseholdPreviewResponse {
  name: string;
  memberCount: number;
  createdAt: string;
  alreadyMember: boolean;
}
