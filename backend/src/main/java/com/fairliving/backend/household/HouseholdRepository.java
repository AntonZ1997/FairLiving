package com.fairliving.backend.household;

import com.fairliving.backend.household.dto.HouseholdSummaryResponse;
import com.fairliving.backend.task.TaskStatus;
import de.fairliving.backend.jooq.tables.HouseholdMember;
import de.fairliving.backend.jooq.tables.records.HouseholdRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.AssignedTask.ASSIGNED_TASK;
import static de.fairliving.backend.jooq.tables.Household.HOUSEHOLD;
import static de.fairliving.backend.jooq.tables.HouseholdMember.HOUSEHOLD_MEMBER;
import static de.fairliving.backend.jooq.tables.Task.TASK;

@Repository
public class HouseholdRepository {

    private final DSLContext dslContext;

    public HouseholdRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public HouseholdRecord insert(String name, String invitationId) {
        HouseholdRecord record = dslContext.newRecord(HOUSEHOLD);
        record.setId(UUID.randomUUID());
        record.setName(name);
        record.setInvitationId(invitationId);
        record.setCreatedAt(Instant.now());
        record.store();
        return record;
    }

    public Optional<HouseholdRecord> findById(UUID id) {
        return dslContext.selectFrom(HOUSEHOLD)
                .where(HOUSEHOLD.ID.eq(id))
                .fetchOptionalInto(HouseholdRecord.class);
    }

    public Optional<HouseholdRecord> findByInvitationId(UUID invitationId) {
        return dslContext.selectFrom(HOUSEHOLD)
                .where(HOUSEHOLD.INVITATION_ID.eq(invitationId.toString()))
                .fetchOptionalInto(HouseholdRecord.class);
    }

    public List<HouseholdSummaryResponse> findByUserId(UUID userId) {
        HouseholdMember member = HOUSEHOLD_MEMBER.as("household_member");

        Field<Integer> memberCount = dslContext.selectCount()
                .from(member)
                .where(member.HOUSEHOLD_ID.eq(HOUSEHOLD.ID))
                .asField("member_count");

        Field<Integer> openTaskCount = dslContext.selectCount()
                .from(ASSIGNED_TASK)
                .join(TASK).on(TASK.ID.eq(ASSIGNED_TASK.TASK_ID))
                .where(TASK.HOUSEHOLD_ID.eq(HOUSEHOLD.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()))
                .and(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(HOUSEHOLD_MEMBER.ID))
                .asField("open_task_count");

        return dslContext
                .select(
                        HOUSEHOLD.ID,
                        HOUSEHOLD.NAME,
                        HOUSEHOLD_MEMBER.ROLE,
                        openTaskCount,
                        memberCount,
                        HOUSEHOLD_MEMBER.JOINED
                )
                .from(HOUSEHOLD_MEMBER)
                .join(HOUSEHOLD).on(HOUSEHOLD.ID.eq(HOUSEHOLD_MEMBER.HOUSEHOLD_ID))
                .where(HOUSEHOLD_MEMBER.USER_ID.eq(userId))
                .orderBy(HOUSEHOLD_MEMBER.JOINED.asc())
                .fetch(r -> new HouseholdSummaryResponse(
                        r.get(HOUSEHOLD.ID),
                        r.get(HOUSEHOLD.NAME),
                        HouseholdRole.fromString(r.get(HOUSEHOLD_MEMBER.ROLE)),
                        r.get(openTaskCount),
                        r.get(memberCount),
                        r.get(HOUSEHOLD_MEMBER.JOINED)
                ));
    }
}
