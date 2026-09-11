package com.fairliving.backend.household;

import com.fairliving.backend.household.dto.*;
import com.fairliving.backend.user.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/households")
public class HouseholdController {
    private final HouseholdService householdService;
    private final CurrentUserProvider currentUserProvider;

    public HouseholdController(HouseholdService householdService, CurrentUserProvider currentUserProvider) {
        this.householdService = householdService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<HouseholdResponse> createHousehold(@Valid @RequestBody CreateHouseholdRequest createHouseholdRequest, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(householdService.createHousehold(createHouseholdRequest, userId));
    }

    @PostMapping("/join")
    public ResponseEntity<HouseholdResponse> joinHousehold(@Valid @RequestBody JoinHouseholdRequest joinHouseholdRequest, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(householdService.joinHousehold(joinHouseholdRequest, userId));
    }

    @GetMapping
    public ResponseEntity<List<HouseholdSummaryResponse>> getUserHouseholds(Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(householdService.getHouseholdsForUser(userId));
    }

    @GetMapping("/{householdId}/members")
    public ResponseEntity<List<HouseholdMemberResponse>> getMembers(@PathVariable UUID householdId, Authentication authentication) {
        UUID userId = currentUserProvider.get(authentication).getId();
        return ResponseEntity.ok(householdService.getMembers(householdId, userId));
    }
}
