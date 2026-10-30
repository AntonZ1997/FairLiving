export interface CompletionShareResponse {
  onTime: number;
  late: number;
  open: number;
}

export interface CumulatedExperiencePoints {
  completedAt: string;
  cumulatedExperiencePoints: number
}

export interface HouseholdMemberCompletionCount {
  householdMemberName: string;
  completedCount: number;
}

export interface HouseholdMemberWorkloadShare {
  householdMemberName: string;
  workload: number;
}
