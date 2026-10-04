package com.fairliving.backend.task;

import de.fairliving.backend.jooq.tables.records.TaskRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.Task.TASK;

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

    public Optional<TaskRecord> findById(UUID id) {
        return dslContext
                .selectFrom(TASK)
                .where(TASK.ID.eq(id))
                .fetchOptionalInto(TaskRecord.class);
    }
}
