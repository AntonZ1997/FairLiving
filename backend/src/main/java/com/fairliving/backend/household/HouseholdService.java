package com.fairliving.backend.household;

import com.fairliving.backend.exception.*;
import com.fairliving.backend.gamification.GamificationService;
import com.fairliving.backend.household.dto.*;
import com.fairliving.backend.notification.NotificationService;
import com.fairliving.backend.task.AssignedTaskRepository;
import com.fairliving.backend.task.TaskDistributionService;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import de.fairliving.backend.jooq.tables.records.HouseholdRecord;
import de.fairliving.backend.jooq.tables.records.LevelRecord;
import jakarta.transaction.Transactional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class HouseholdService {

    private static final int INITIAL_EXPERIENCE_POINTS = 0;

    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final GamificationService gamificationService;
    private final AssignedTaskRepository assignedTaskRepository;
    private final TaskDistributionService taskDistributionService;
    private final NotificationService notificationService;

    public HouseholdService(HouseholdRepository householdRepository, HouseholdMemberRepository householdMemberRepository, GamificationService gamificationService, AssignedTaskRepository assignedTaskRepository, TaskDistributionService taskDistributionService, NotificationService notificationService) {
        this.householdRepository = householdRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.gamificationService = gamificationService;
        this.assignedTaskRepository = assignedTaskRepository;
        this.taskDistributionService = taskDistributionService;
        this.notificationService = notificationService;
    }

    @Transactional
    public HouseholdResponse createHousehold(CreateHouseholdRequest createHouseholdRequest, UUID userId) {
        HouseholdRecord household = householdRepository.insert(createHouseholdRequest.name(), generateInvitationId());
        addMember(household.getId(), userId, HouseholdRole.ADMIN);

        return HouseholdResponse.from(household);
    }

    @Transactional
    public HouseholdResponse joinHousehold(JoinHouseholdRequest joinHouseholdRequest, UUID userId) {
        HouseholdRecord household = householdRepository.findByInvitationId(joinHouseholdRequest.invitationId())
                .orElseThrow(InvalidInvitationIdException::new);

        if (householdMemberRepository.exists(household.getId(), userId)) {
            throw new UserIsAlreadyHouseholdMemberException(household.getId(), userId);
        }

        try {
            UUID newHouseholdMemberId = addMember(household.getId(), userId, HouseholdRole.MEMBER);
            notificationService.notifyNewHouseholdMember(household.getId(), newHouseholdMemberId);
        } catch (DuplicateKeyException e) {
            throw new UserIsAlreadyHouseholdMemberException(household.getId(), userId);
        }

        return HouseholdResponse.from(household);
    }

    public List<HouseholdSummaryResponse> getHouseholdsForUser(UUID userId) {
        return householdRepository.findByUserId(userId);
    }

    public List<HouseholdMemberResponse> getMembers(UUID householdId, UUID userId) {
        requireMembership(householdId, userId);
        return householdMemberRepository.findMembers(householdId, userId);
    }

    public HouseholdPreviewResponse getHouseholdPreview(UUID invitationId, UUID userId) {
        HouseholdRecord household = householdRepository.findByInvitationId(invitationId)
                .orElseThrow(InvalidInvitationIdException::new);

        return new HouseholdPreviewResponse(
                household.getName(),
                householdMemberRepository.countMembersByHouseholdId(household.getId()),
                household.getCreatedAt(),
                householdMemberRepository.exists(household.getId(), userId)
        );
    }

    public HouseholdResponse getHousehold(UUID householdId, UUID userId) {
        requireMembership(householdId, userId);

        return householdRepository.findById(householdId)
                .map(HouseholdResponse::from)
                .orElseThrow(() -> new UserIsNotAHouseholdMemberException(householdId, userId));
    }

    public HouseholdMemberRecord requireMembership(UUID householdId, UUID userId) {
        return householdMemberRepository.findMembership(householdId, userId)
                .orElseThrow(() -> new UserIsNotAHouseholdMemberException(householdId, userId));
    }

    public HouseholdMemberRecord requireAdmin(UUID householdId, UUID userId) {
        HouseholdMemberRecord membership = requireMembership(householdId, userId);
        if (!isAdmin(membership)) {
            throw new UserIsNotHouseholdAdminException(householdId, userId);
        }
        return membership;
    }

    /**
     * Entfernt ein Mitglied aus dem Haushalt, wenn der angemeldete Nutzer ein Admin ist und das zu entfernende
     * Mitglied nicht man selbst ist. Dem zu löschenden Nutzer zugewiesene Aufgaben werden auf die verbleibenden
     * Haushaltsmitglieder mittels des dynamischen Verteilungsalgorithmus verteilt.
     * @param householdId ID des Haushalts
     * @param memberId ID des zu löschenden Nutzers
     * @param currentUserId ID des angemeldeten Nutzers
     */
    @Transactional
    public void removeMember(UUID householdId, UUID memberId, UUID currentUserId) {
        HouseholdMemberRecord admin = requireAdmin(householdId, currentUserId);

        if (admin.getId().equals(memberId)) {
            throw new CannotRemoveSelfException(memberId);
        }

        requireMemberOfHousehold(householdId, memberId);

        String removedHouseholdMemberName = householdMemberRepository.findUserNameByMemberId(memberId)
                .orElse("Ein Mitglied");

        redistributeOpenTasks(householdId, memberId);

        householdMemberRepository.delete(memberId);

        notificationService.notifyRemovedMember(householdId, removedHouseholdMemberName);
    }

    /**
     * Überträgt Adminrechte auf ein anderes Haushaltsmitglied. Der alte Admin wird ein normales Haushaltsmitglied.
     * @param householdId ID des Haushalts
     * @param newAdminMemberId ID des neuen Admins
     * @param currentUserId ID des angemeldeten Nutzers
     */
    @Transactional
    public void changeAdmin(UUID householdId, UUID newAdminMemberId, UUID currentUserId) {
        HouseholdMemberRecord admin = requireAdmin(householdId, currentUserId);

        if (admin.getId().equals(newAdminMemberId)) {
            return;
        }

        requireMemberOfHousehold(householdId, newAdminMemberId);

        householdMemberRepository.updateRole(newAdminMemberId, HouseholdRole.ADMIN);
        householdMemberRepository.updateRole(admin.getId(), HouseholdRole.MEMBER);

        notificationService.notifyNewAdmin(newAdminMemberId);
    }

    /**
     * Entfernt den angemeldeten Nutzer aus dem Haushalt. War er Admin des Haushalts, gehen die Adminrechte an das verbleibende
     * Mitglied, welches nach ihm am längsten im Haushalt ist. Seine Aufgaben werden auf alle verbleibenden Haushaltsmitglieder
     * mittels des dynamischen Verteilungsalgorithmus verteilt.
     * Falls er das letzte Mitglied des Haushalts war, wird der Haushalt mitsamt aller Aufgaben gelöscht.
     * @param householdId ID des Haushalts
     * @param userId ID des angemeldeten Nutzers
     */
    @Transactional
    public void leaveHousehold(UUID householdId, UUID userId) {
        HouseholdMemberRecord leavingMember = requireMembership(householdId, userId);

        Optional<UUID> successorMemberId =
                householdMemberRepository.findOldestMember(householdId, leavingMember.getId());

        if (successorMemberId.isEmpty()) {
            householdMemberRepository.delete(leavingMember.getId());
            householdRepository.delete(householdId);
            return;
        }

        if (isAdmin(leavingMember)) {
            householdMemberRepository.updateRole(successorMemberId.get(), HouseholdRole.ADMIN);
            notificationService.notifyNewAdmin(successorMemberId.get());
        }

        String leavingHouseholdMemberName = householdMemberRepository.findUserNameByMemberId(leavingMember.getId())
                .orElse("Ein Mitglied");

        redistributeOpenTasks(householdId, leavingMember.getId());

        householdMemberRepository.delete(leavingMember.getId());

        notificationService.notifyRemovedMember(householdId, leavingHouseholdMemberName);
    }

    private void redistributeOpenTasks(UUID householdId, UUID householdMemberId) {
        for (UUID assignedTaskId : assignedTaskRepository.findOpenAssignedTaskIdsByMemberId(householdMemberId)) {
            UUID otherHouseholdMember = taskDistributionService.determineHouseholdMemberForTask(householdId, householdMemberId);
            assignedTaskRepository.reassign(assignedTaskId, otherHouseholdMember);
        }
    }

    private void requireMemberOfHousehold(UUID householdId, UUID memberId) {
        householdMemberRepository.findById(memberId)
                .filter(member -> member.getHouseholdId().equals(householdId))
                .orElseThrow(() -> new UserIsNotAHouseholdMemberException(householdId, memberId));
    }

    private boolean isAdmin(HouseholdMemberRecord householdMember) {
        return HouseholdRole.fromString(householdMember.getRole()).equals(HouseholdRole.ADMIN);
    }

    private UUID addMember(UUID householdId, UUID userId, HouseholdRole householdRole) {
        LevelRecord startLevel = gamificationService.resolveLevel(INITIAL_EXPERIENCE_POINTS);
        return householdMemberRepository.insert(householdId, userId, householdRole, startLevel.getId(), INITIAL_EXPERIENCE_POINTS);
    }

    private String generateInvitationId() {
        return UUID.randomUUID().toString();
    }
}