package com.fairliving.backend.notification;

import com.fairliving.backend.exception.UserIsNotAHouseholdMemberException;
import com.fairliving.backend.household.HouseholdMemberRepository;
import com.fairliving.backend.notification.dto.NotificationCreatedEvent;
import com.fairliving.backend.notification.dto.NotificationResponse;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Erstellt Benachrichtigungen für Ereignisse in einem Haushalt und markiert diese als gelesen
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final HouseholdMemberRepository householdMemberRepository;
    private final NotificationSendingService notificationSendingService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public NotificationService(NotificationRepository notificationRepository, HouseholdMemberRepository householdMemberRepository, NotificationSendingService notificationSendingService, ApplicationEventPublisher applicationEventPublisher) {
        this.notificationRepository = notificationRepository;
        this.householdMemberRepository = householdMemberRepository;
        this.notificationSendingService = notificationSendingService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public SseEmitter streamNotifications(UUID householdId, UUID userId) {
        HouseholdMemberRecord householdMember = requireMembership(householdId, userId);

        List<NotificationResponse> notifications =
                notificationRepository.findNewNotificationsByHouseholdMember(householdMember.getId());

        return notificationSendingService.subscribe(householdMember.getId(), notifications);
    }

    @Transactional
    public void readNotifications(UUID householdId, UUID userId, List<UUID> notificationIds) {
        HouseholdMemberRecord householdMember = requireMembership(householdId, userId);
        notificationRepository.setOnReadByNotificationIds(householdMember.getId(), notificationIds);
    }

    public void notifyAssignedTask(UUID householdMemberId, String taskName) {
        send(List.of(householdMemberId), NotificationType.INFO,
                "Die Aufgabe '" + taskName + "' wurde dir zugewiesen.");
    }

    public void notifyDueTodayTask(UUID householdMemberId, String taskName) {
        send(List.of(householdMemberId), NotificationType.WARNING,
                "Die Aufgabe '" + taskName + "' ist heute fällig.");
    }

    public void notifyOverdueTask(UUID householdMemberId, String taskName) {
        send(List.of(householdMemberId), NotificationType.ALERT,
                "Die Aufgabe '" + taskName + "' ist überfällig.");
    }

    public void notifyNewLevel(UUID householdId, UUID householdMemberId, int levelNumber, String levelTitle) {
        String householdMemberName = resolveHouseholdMemberName(householdMemberId);

        send(householdMemberRepository.findHouseholdMemberIdsByHouseholdId(householdId), NotificationType.INFO,
                householdMemberName + " hat Level " + levelNumber + " erreicht: " + levelTitle + ".");
    }

    public void notifyNewHouseholdMember(UUID householdId, UUID newHouseholdMemberId) {
        String householdMemberName = resolveHouseholdMemberName(newHouseholdMemberId);

        List<UUID> recipients = householdMemberRepository.findHouseholdMemberIdsByHouseholdId(householdId).stream()
                .filter(householdMemberId -> !householdMemberId.equals(newHouseholdMemberId))
                .toList();

        send(recipients, NotificationType.INFO, householdMemberName + " ist dem Haushalt beigetreten.");
    }

    public void notifyRemovedMember(UUID householdId, String removedHouseholdMemberName) {
        send(householdMemberRepository.findHouseholdMemberIdsByHouseholdId(householdId), NotificationType.INFO,
                removedHouseholdMemberName + " ist nicht mehr Mitglied des Haushalts. Offene Aufgaben wurden neu verteilt.");
    }

    public void notifyNewAdmin(UUID householdMemberId) {
        send(List.of(householdMemberId), NotificationType.INFO,
                "Du bist jetzt Admin des Haushalts.");
    }

    /**
     * Speichert Benachrichtigung für alle Empfänger und initiiert eine Versendung an die Haushaltsmitglieder.
     * @param householdMemberIds Liste der IDs der Haushaltsmitglieder
     * @param type Benachrichtigungs-Typ
     * @param message Benachrichtigung
     */
    private void send(List<UUID> householdMemberIds, NotificationType type, String message) {
        if (householdMemberIds.isEmpty()) {
            return;
        }

        notificationRepository.insertAll(householdMemberIds, type, message);
        applicationEventPublisher.publishEvent(new NotificationCreatedEvent(Set.copyOf(householdMemberIds)));
    }

    private String resolveHouseholdMemberName(UUID householdMemberId) {
        return householdMemberRepository.findUserNameByMemberId(householdMemberId).orElse("Ein Mitglied");
    }

    private HouseholdMemberRecord requireMembership(UUID householdId, UUID userId) {
        return householdMemberRepository.findMembership(householdId, userId).orElseThrow(
                () -> new UserIsNotAHouseholdMemberException(householdId, userId)
        );
    }
}