package com.fairliving.backend.notification;

import com.fairliving.backend.notification.dto.NotificationResponse;
import de.fairliving.backend.jooq.tables.records.NotificationRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.Notification.NOTIFICATION;

@Repository
public class NotificationRepository {

    private final DSLContext dslContext;

    public NotificationRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<NotificationResponse> findNewNotificationsByHouseholdMember(UUID householdMemberId) {
        return dslContext
                .select(
                        NOTIFICATION.ID,
                        NOTIFICATION.TYPE,
                        NOTIFICATION.MESSAGE,
                        NOTIFICATION.CREATED_AT
                )
                .from(NOTIFICATION)
                .where(NOTIFICATION.HOUSEHOLD_MEMBER_ID.eq(householdMemberId))
                .and(NOTIFICATION.READ.isFalse())
                .orderBy(NOTIFICATION.CREATED_AT.desc())
                .fetch(r -> new NotificationResponse(
                        r.get(NOTIFICATION.ID),
                        NotificationType.fromString(r.get(NOTIFICATION.TYPE)),
                        r.get(NOTIFICATION.MESSAGE),
                        r.get(NOTIFICATION.CREATED_AT)
                ));
    }


    public void setOnReadByNotificationIds(UUID householdMemberId, List<UUID> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }

        dslContext
                .update(NOTIFICATION)
                .set(NOTIFICATION.READ, true)
                .where(NOTIFICATION.HOUSEHOLD_MEMBER_ID.eq(householdMemberId))
                .and(NOTIFICATION.ID.in(notificationIds))
                .execute();
    }

    public void insertAll(List<UUID> householdMemberIds, NotificationType type, String message) {
        if (householdMemberIds.isEmpty()) {
            return;
        }

        Instant createdAt = Instant.now();

        List<NotificationRecord> notifications = householdMemberIds.stream()
                .map(householdMemberId -> {
                    NotificationRecord record = dslContext.newRecord(NOTIFICATION);
                    record.setId(UUID.randomUUID());
                    record.setHouseholdMemberId(householdMemberId);
                    record.setType(type.getType());
                    record.setMessage(message);
                    record.setCreatedAt(createdAt);
                    record.setRead(false);
                    return record;
                }).toList();

        dslContext.batchInsert(notifications).execute();
    }

}
