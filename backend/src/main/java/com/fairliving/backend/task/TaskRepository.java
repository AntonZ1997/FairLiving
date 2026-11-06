package com.fairliving.backend.task;

import com.fairliving.backend.task.dto.TaskDetailResponse;
import com.fairliving.backend.task.dto.TaskListItemResponse;
import de.fairliving.backend.jooq.tables.AssignedTask;
import de.fairliving.backend.jooq.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.AssignedTask.ASSIGNED_TASK;
import static de.fairliving.backend.jooq.tables.Difficulty.DIFFICULTY;
import static de.fairliving.backend.jooq.tables.HouseholdMember.HOUSEHOLD_MEMBER;
import static de.fairliving.backend.jooq.tables.Task.TASK;
import static de.fairliving.backend.jooq.tables.User.USER;

@Repository
public class TaskRepository {

    private final DSLContext dslContext;

    public TaskRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public TaskRecord insert(UUID householdId, String name, String description, UUID difficultyId, int intervalDays) {
        TaskRecord taskRecord = dslContext.newRecord(TASK);
        taskRecord.setId(UUID.randomUUID());
        taskRecord.setHouseholdId(householdId);
        taskRecord.setName(name);
        taskRecord.setDescription(description);
        taskRecord.setDifficultyId(difficultyId);
        taskRecord.setIntervalDays(intervalDays);
        taskRecord.setActive(true);
        taskRecord.store();
        return taskRecord;
    }

    public void update(UUID taskId, String name, String description, UUID difficultyId, int intervalDays) {
        dslContext.update(TASK)
                .set(TASK.NAME, name)
                .set(TASK.DESCRIPTION, description)
                .set(TASK.DIFFICULTY_ID, difficultyId)
                .set(TASK.INTERVAL_DAYS, intervalDays)
                .where(TASK.ID.eq(taskId))
                .execute();
    }

    public Optional<TaskRecord> findById(UUID id) {
        return dslContext
                .selectFrom(TASK)
                .where(TASK.ID.eq(id))
                .fetchOptionalInto(TaskRecord.class);
    }

    public void setActive(UUID taskId, boolean active) {
        dslContext.update(TASK)
                .set(TASK.ACTIVE, active)
                .where(TASK.ID.eq(taskId))
                .execute();
    }

    public Optional<TaskDetailResponse> fetchTaskDetails(UUID taskId, UUID currentMemberId) {
        AssignedTask completedAssignedTask = ASSIGNED_TASK.as("completedAssignedTask");

        Field<Integer> completedByCurrentUser = dslContext
                .selectCount()
                .from(completedAssignedTask)
                .where(completedAssignedTask.TASK_ID.eq(TASK.ID))
                .and(completedAssignedTask.ASSIGNED_MEMBER_ID.eq(currentMemberId))
                .and(completedAssignedTask.STATUS.eq(TaskStatus.COMPLETED.getStatus()))
                .asField("completedByCurrentUser");

        return dslContext
                .select(
                        TASK.ID,
                        TASK.NAME,
                        TASK.DESCRIPTION,
                        TASK.INTERVAL_DAYS,
                        TASK.ACTIVE,
                        DIFFICULTY.ID,
                        DIFFICULTY.NAME,
                        DIFFICULTY.BASE_XP,
                        ASSIGNED_TASK.ID,
                        ASSIGNED_TASK.ASSIGNED_MEMBER_ID,
                        ASSIGNED_TASK.DUE_DATE,
                        USER.USER_NAME,
                        completedByCurrentUser
                ).from(TASK)
                .join(DIFFICULTY).on(DIFFICULTY.ID.eq(TASK.DIFFICULTY_ID))
                .leftJoin(ASSIGNED_TASK).on(ASSIGNED_TASK.TASK_ID.eq(TASK.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()))
                .leftJoin(HOUSEHOLD_MEMBER).on(HOUSEHOLD_MEMBER.ID.eq(ASSIGNED_TASK.ASSIGNED_MEMBER_ID))
                .leftJoin(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .where(TASK.ID.eq(taskId))
                .fetchOptional(r -> new TaskDetailResponse(
                        r.get(TASK.ID),
                        r.get(TASK.NAME),
                        r.get(TASK.DESCRIPTION),
                        r.get(DIFFICULTY.ID),
                        r.get(DIFFICULTY.NAME),
                        r.get(DIFFICULTY.BASE_XP),
                        r.get(TASK.INTERVAL_DAYS),
                        r.get(TASK.ACTIVE),
                        r.get(ASSIGNED_TASK.ID),
                        r.get(USER.USER_NAME),
                        r.get(ASSIGNED_TASK.DUE_DATE),
                        currentMemberId.equals(r.get(ASSIGNED_TASK.ASSIGNED_MEMBER_ID)),
                        r.get(completedByCurrentUser)
                ));
    }

    public List<TaskListItemResponse> findTaskListItemsByHouseholdId(UUID householdId, boolean active) {
        return dslContext
                .select(
                        TASK.ID,
                        TASK.NAME,
                        DIFFICULTY.NAME,
                        DIFFICULTY.WEIGHT,
                        TASK.INTERVAL_DAYS,
                        ASSIGNED_TASK.DUE_DATE,
                        USER.USER_NAME
                ).from(TASK)
                .join(DIFFICULTY).on(DIFFICULTY.ID.eq(TASK.DIFFICULTY_ID))
                .leftJoin(ASSIGNED_TASK).on(ASSIGNED_TASK.TASK_ID.eq(TASK.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()))
                .leftJoin(HOUSEHOLD_MEMBER).on(HOUSEHOLD_MEMBER.ID.eq(ASSIGNED_TASK.ASSIGNED_MEMBER_ID))
                .leftJoin(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .where(TASK.HOUSEHOLD_ID.eq(householdId))
                .and(TASK.ACTIVE.eq(active))
                .orderBy(ASSIGNED_TASK.DUE_DATE.asc().nullsLast(), TASK.NAME.asc())
                .fetch(r -> new TaskListItemResponse(
                        r.get(TASK.ID),
                        r.get(TASK.NAME),
                        r.get(DIFFICULTY.NAME),
                        r.get(DIFFICULTY.WEIGHT),
                        r.get(TASK.INTERVAL_DAYS),
                        r.get(ASSIGNED_TASK.DUE_DATE),
                        r.get(USER.USER_NAME)
                ));
    }
}
