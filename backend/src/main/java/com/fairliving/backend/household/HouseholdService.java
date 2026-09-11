package com.fairliving.backend.household;

import com.fairliving.backend.exception.InvalidInvitationTokenException;
import com.fairliving.backend.exception.UserIsAlreadyHouseholdMemberException;
import com.fairliving.backend.exception.UserIsNotAHouseholdMemberException;
import com.fairliving.backend.exception.UserIsNotHouseholdAdminException;
import com.fairliving.backend.gamification.GamificationService;
import com.fairliving.backend.household.dto.*;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import de.fairliving.backend.jooq.tables.records.HouseholdRecord;
import de.fairliving.backend.jooq.tables.records.LevelRecord;
import jakarta.transaction.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class HouseholdService {

    private static final int INITIAL_EXPERIENCE_POINTS = 0;

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final GamificationService gamificationService;

    public HouseholdService(HouseholdRepository householdRepository,  HouseholdMemberRepository householdMemberRepository, GamificationService gamificationService) {
         this.householdRepository = householdRepository;
         this.householdMemberRepository = householdMemberRepository;
         this.gamificationService = gamificationService;
    }

    @Transactional
    public HouseholdResponse createHousehold(CreateHouseholdRequest createHouseholdRequest, UUID userId) {
        HouseholdRecord household = householdRepository.insert(createHouseholdRequest.name(), generateInvitationToken());
        addMember(household.getId(), userId, HouseholdRole.ADMIN);

        return HouseholdResponse.from(household);
    }

    public HouseholdResponse joinHousehold(JoinHouseholdRequest joinHouseholdRequest, UUID userId) {
        HouseholdRecord household = householdRepository.findByInvitationToken(joinHouseholdRequest.invitationToken())
                .orElseThrow(InvalidInvitationTokenException::new);

        if(householdMemberRepository.exists(household.getId(), userId)) {
            throw new UserIsAlreadyHouseholdMemberException(household.getId(), userId);
        }

        try {
            addMember(household.getId(), userId, HouseholdRole.MEMBER);
        } catch(DuplicateKeyException e) {
            throw new UserIsAlreadyHouseholdMemberException(household.getId(), userId);
        }

        return HouseholdResponse.from(household);
    }

    public List<HouseholdSummaryResponse> getHouseholdsForUser(UUID userId) {
        return householdRepository.findByUserId(userId);
    }

    public List<HouseholdMemberResponse> getMembers(UUID householdId, UUID userId) {
        requireMembership(householdId, userId);
        return householdMemberRepository.findMembers(householdId);
    }

    private HouseholdMemberRecord requireMembership(UUID householdId, UUID userId) {
        return householdMemberRepository.findMembership(householdId, userId)
                .orElseThrow(() -> new UserIsNotAHouseholdMemberException(householdId, userId));
    }

    private HouseholdMemberRecord requireAdmin(UUID householdId, UUID userId) {
        HouseholdMemberRecord membership = requireMembership(householdId, userId);
        if(HouseholdRole.fromString(membership.getRole()).equals(HouseholdRole.ADMIN)) {
            throw new UserIsNotHouseholdAdminException(householdId, userId);
        }
        return membership;
    }


    private void addMember(UUID householdId, UUID userId, HouseholdRole householdRole) {
        LevelRecord startLevel = gamificationService.resolveLevel(INITIAL_EXPERIENCE_POINTS);
        householdMemberRepository.insert(householdId, userId, householdRole, startLevel.getId(), INITIAL_EXPERIENCE_POINTS);

    }

    private String generateInvitationToken() {
        return UUID.randomUUID().toString();
    }
}
