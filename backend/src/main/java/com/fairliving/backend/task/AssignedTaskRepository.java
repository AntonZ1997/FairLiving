package com.fairliving.backend.task;

import com.fairliving.backend.task.dto.AssignedTaskResponse;
import com.fairliving.backend.task.dto.MemberWorkload;
import de.fairliving.backend.jooq.tables.records.AssignedTaskRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.Tables.USER;
import static de.fairliving.backend.jooq.tables.AssignedTask.ASSIGNED_TASK;
import static de.fairliving.backend.jooq.tables.Difficulty.DIFFICULTY;
import static de.fairliving.backend.jooq.tables.HouseholdMember.HOUSEHOLD_MEMBER;
import static de.fairliving.backend.jooq.tables.Task.TASK;

@Repository
public class AssignedTaskRepository {

    private final DSLContext dslContext;

    public AssignedTaskRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public AssignedTaskRecord insert(UUID taskId, UUID assignedMemberId, Instant dueDate) {
        AssignedTaskRecord assignedTaskRecord = dslContext.newRecord(ASSIGNED_TASK);
        assignedTaskRecord.setId(UUID.randomUUID());
        assignedTaskRecord.setTaskId(taskId);
        assignedTaskRecord.setAssignedMemberId(assignedMemberId);
        assignedTaskRecord.setDueDate(dueDate);
        assignedTaskRecord.setStatus(TaskStatus.OPEN.getStatus());
        assignedTaskRecord.store();
        return assignedTaskRecord;
    }

    public List<AssignedTaskResponse> findOpenTasksByMemberId(UUID assignedMemberId) {
        return find(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(assignedMemberId)
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus())));
    }

    public Optional<AssignedTaskRecord> findRecordById(UUID assignedTaskId) {
        return dslContext
                .selectFrom(ASSIGNED_TASK)
                .where(ASSIGNED_TASK.ID.eq(assignedTaskId))
                .fetchOptionalInto(AssignedTaskRecord.class);
    }

    public Optional<AssignedTaskResponse> findById(UUID assignedTaskId) {
        return find(ASSIGNED_TASK.ID.eq(assignedTaskId)).stream().findFirst();
    }

    private List<AssignedTaskResponse> find(Condition condition) {
        return dslContext
                .select(
                        ASSIGNED_TASK.ID,
                        TASK.NAME,
                        TASK.DESCRIPTION,
                        DIFFICULTY.NAME,
                        DIFFICULTY.BASE_XP,
                        ASSIGNED_TASK.DUE_DATE,
                        USER.USER_NAME
                ).from(ASSIGNED_TASK)
                .join(TASK).on(TASK.ID.eq(ASSIGNED_TASK.TASK_ID))
                .join(DIFFICULTY).on(DIFFICULTY.ID.eq(TASK.DIFFICULTY_ID))
                .join(HOUSEHOLD_MEMBER).on(HOUSEHOLD_MEMBER.ID.eq(ASSIGNED_TASK.ASSIGNED_MEMBER_ID))
                .join(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .where(condition)
                .orderBy(ASSIGNED_TASK.DUE_DATE.asc())
                .fetch(r -> new AssignedTaskResponse(
                        r.get(ASSIGNED_TASK.ID),
                        r.get(TASK.NAME),
                        r.get(TASK.DESCRIPTION),
                        r.get(DIFFICULTY.NAME),
                        r.get(DIFFICULTY.BASE_XP),
                        r.get(ASSIGNED_TASK.DUE_DATE),
                        r.get(USER.USER_NAME)));
    }

    public List<MemberWorkload> findWorkloads(UUID householdId, Instant completedSince) {
        Field<Double> statusFactor = DSL
                .when(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()),
                        WorkloadFactor.OPEN.getFactor())
                .when(ASSIGNED_TASK.COMPLETED_AT.le(ASSIGNED_TASK.DUE_DATE),
                        WorkloadFactor.COMPLETED_ON_TIME.getFactor())
                .otherwise(WorkloadFactor.COMPLETED_LATE.getFactor());

        Field<BigDecimal> workload = DSL.coalesce(
                DSL.sum(statusFactor.mul(DIFFICULTY.WEIGHT)), BigDecimal.ZERO);

        Field<Integer> openTaskCount = DSL.count(ASSIGNED_TASK.ID)
                .filterWhere(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()));

        return dslContext
                .select(HOUSEHOLD_MEMBER.ID, workload, openTaskCount)
                .from(HOUSEHOLD_MEMBER)
                .leftJoin(ASSIGNED_TASK)
                .on(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(HOUSEHOLD_MEMBER.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus())
                        .or(ASSIGNED_TASK.COMPLETED_AT.gt(completedSince)))
                .leftJoin(TASK).on(TASK.ID.eq(ASSIGNED_TASK.TASK_ID))
                .leftJoin(DIFFICULTY).on(DIFFICULTY.ID.eq(TASK.DIFFICULTY_ID))
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .groupBy(HOUSEHOLD_MEMBER.ID)
                .fetch(r -> new MemberWorkload(
                        r.get(HOUSEHOLD_MEMBER.ID),
                        r.get(workload).doubleValue(),
                        r.get(openTaskCount)));
    }

    public void complete(UUID assignedTaskId, Instant completedAt) {
        dslContext
                .update(ASSIGNED_TASK)
                .set(ASSIGNED_TASK.STATUS, TaskStatus.COMPLETED.getStatus())
                .set(ASSIGNED_TASK.COMPLETED_AT, completedAt)
                .where(ASSIGNED_TASK.ID.eq(assignedTaskId))
                .execute();
    }

}
