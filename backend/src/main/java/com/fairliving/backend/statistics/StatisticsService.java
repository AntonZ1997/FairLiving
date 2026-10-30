package com.fairliving.backend.statistics;

import com.fairliving.backend.exception.InvalidDateRangeException;
import com.fairliving.backend.household.HouseholdService;
import com.fairliving.backend.statistics.dto.CompletionShareResponse;
import com.fairliving.backend.statistics.dto.CumulatedExperiencePoints;
import com.fairliving.backend.statistics.dto.HouseholdMemberCompletionCount;
import com.fairliving.backend.statistics.dto.HouseholdMemberWorkloadShare;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final HouseholdService householdService;

    public StatisticsService(StatisticsRepository statisticsRepository, HouseholdService householdService) {
        this.statisticsRepository = statisticsRepository;
        this.householdService = householdService;
    }

    public List<HouseholdMemberCompletionCount> getHouseholdMemberCompletionCounts(UUID householdId, UUID userId, Instant from, Instant to) {
        householdService.requireMembership(householdId, userId);
        checkValidRange(from, to);
        return statisticsRepository.findHouseholdMemberCompletionCounts(householdId, from, to);
    }

    public CompletionShareResponse getCompletionShare(UUID householdId, UUID userId, Instant from, Instant to) {
        HouseholdMemberRecord member = householdService.requireMembership(householdId, userId);
        checkValidRange(from, to);
        return statisticsRepository.findHouseholdMemberCompletionShares(member.getId(), from, to);
    }

    public List<CumulatedExperiencePoints> getCumulatedExperiencePoints(UUID householdId, UUID userId, Instant from, Instant to) {
        HouseholdMemberRecord member = householdService.requireMembership(householdId, userId);
        checkValidRange(from, to);
        return statisticsRepository.findCumulatedExperiencePoints(member.getId(), from, to);
    }

    public List<HouseholdMemberWorkloadShare> getHouseholdMemberWorkloadShares(UUID householdId, UUID userId, Instant from, Instant to) {
        householdService.requireMembership(householdId, userId);
        checkValidRange(from, to);
        return statisticsRepository.findHouseholdMemberWorkloadShares(householdId, from);
    }

    private void checkValidRange(Instant from, Instant to) {
        if(to.isAfter(Instant.now()) || from.isAfter(to)) {
            throw new InvalidDateRangeException(from, to);
        }
    }
}
