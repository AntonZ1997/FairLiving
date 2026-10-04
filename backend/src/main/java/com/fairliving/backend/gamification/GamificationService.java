package com.fairliving.backend.gamification;

import com.fairliving.backend.gamification.dto.CompletionReward;
import com.fairliving.backend.household.HouseholdMemberRepository;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import de.fairliving.backend.jooq.tables.records.LevelRecord;
import org.springframework.stereotype.Service;

@Service
public class GamificationService {

    private static final double STREAK_BONUS_DIVISOR = 100.0;
    private static final double MAX_STREAK_BONUS = 1.0;

    private final LevelRepository levelRepository;
    private final HouseholdMemberRepository householdMemberRepository;

    public GamificationService(LevelRepository levelRepository, HouseholdMemberRepository householdMemberRepository) {
        this.levelRepository = levelRepository;
        this.householdMemberRepository = householdMemberRepository;
    }

    public LevelRecord resolveLevel(int experiencePoints) {
        return levelRepository.findByXp(experiencePoints)
                .orElseThrow(() -> new IllegalStateException(
                        "No level defined for XP: "  + experiencePoints
                ));
    }

    public CompletionReward calculateCompletionReward(HouseholdMemberRecord householdMember, int earnedBaseXp, boolean completedOnTime) {
        int streakCount = completedOnTime ? householdMember.getStreakCount() : 0;
        int earnedXp = calculateExperiencePoints(earnedBaseXp, streakCount);
        int totalXp = householdMember.getExperiencePoints() + earnedXp;
        streakCount = completedOnTime ? streakCount + 1 : 0;

        LevelRecord newLevel = resolveLevel(totalXp);
        boolean leveledUp = !newLevel.getId().equals(householdMember.getLevelId());

        householdMemberRepository.updateProgress(householdMember.getId(), totalXp, streakCount, newLevel.getId());

        return new CompletionReward(earnedXp, totalXp, streakCount, newLevel, leveledUp);
    }

    private int calculateExperiencePoints(int baseXp, int streakCount) {
        double multiplier = 1 + Math.min(streakCount / STREAK_BONUS_DIVISOR, MAX_STREAK_BONUS);
        return Math.toIntExact(Math.round(baseXp * multiplier));
    }
}
