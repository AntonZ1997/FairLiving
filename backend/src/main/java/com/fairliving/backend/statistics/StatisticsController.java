package com.fairliving.backend.statistics;

import com.fairliving.backend.statistics.dto.CompletionShareResponse;
import com.fairliving.backend.statistics.dto.CumulatedExperiencePoints;
import com.fairliving.backend.statistics.dto.HouseholdMemberCompletionCount;
import com.fairliving.backend.statistics.dto.HouseholdMemberWorkloadShare;
import com.fairliving.backend.user.CurrentUserProvider;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/households/{householdId}/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final CurrentUserProvider currentUserProvider;

    public StatisticsController(StatisticsService statisticsService, CurrentUserProvider currentUserProvider) {
        this.statisticsService = statisticsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/completed-tasks")
    public ResponseEntity<List<HouseholdMemberCompletionCount>> getCompletedTasks(
            @PathVariable("householdId") UUID householdId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(statisticsService.getHouseholdMemberCompletionCounts(householdId, userId, from, to));
    }

    @GetMapping("/completion-shares")
    public ResponseEntity<CompletionShareResponse> getCompletionShares(
            @PathVariable UUID householdId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication authentication
    ) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(statisticsService.getCompletionShare(householdId, userId, from, to));
    }

    @GetMapping("/experience-points")
    public ResponseEntity<List<CumulatedExperiencePoints>> getExperiencePoints(
            @PathVariable UUID householdId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication authentication
    ) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(statisticsService.getCumulatedExperiencePoints(householdId, userId, from, to));
    }

    @GetMapping("/workload")
    public ResponseEntity<List<HouseholdMemberWorkloadShare>> getWorkloads(
            @PathVariable UUID householdId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            Authentication authentication
    ) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(statisticsService.getHouseholdMemberWorkloadShares(householdId, userId, from, to));
    }



}
