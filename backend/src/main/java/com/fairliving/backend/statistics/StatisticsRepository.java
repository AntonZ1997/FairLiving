package com.fairliving.backend.statistics;

import com.fairliving.backend.statistics.dto.CompletionShareResponse;
import com.fairliving.backend.statistics.dto.CumulatedExperiencePoints;
import com.fairliving.backend.statistics.dto.HouseholdMemberCompletionCount;
import com.fairliving.backend.statistics.dto.HouseholdMemberWorkloadShare;
import com.fairliving.backend.task.TaskStatus;
import com.fairliving.backend.task.WorkloadFactor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static de.fairliving.backend.jooq.Tables.*;

@Repository
public class StatisticsRepository {

    private final DSLContext dslContext;

    public StatisticsRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<HouseholdMemberCompletionCount> findHouseholdMemberCompletionCounts(UUID householdId, Instant from, Instant to) {
        Field<Integer> completedCount = DSL.count(ASSIGNED_TASK.ID);

        return dslContext
                .select(USER.USER_NAME, completedCount)
                .from(HOUSEHOLD_MEMBER)
                .join(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .leftJoin(ASSIGNED_TASK)
                .on(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(HOUSEHOLD_MEMBER.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus()))
                .and(ASSIGNED_TASK.COMPLETED_AT.between(from, to))
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .groupBy(HOUSEHOLD_MEMBER.ID, USER.USER_NAME)
                .orderBy(completedCount.desc(), USER.USER_NAME.asc())
                .fetch(r -> new HouseholdMemberCompletionCount(
                        r.get(USER.USER_NAME),
                        r.get(completedCount)
                ));
    }

    public CompletionShareResponse findHouseholdMemberCompletionShares(UUID householdMemberId, Instant from, Instant to) {
        Field<Integer> onTime = DSL.count().filterWhere(
                ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus())
                        .and(ASSIGNED_TASK.COMPLETED_AT.le(ASSIGNED_TASK.DUE_DATE)));

        Field<Integer> late = DSL.count().filterWhere(
                ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus())
                        .and(ASSIGNED_TASK.COMPLETED_AT.gt(ASSIGNED_TASK.DUE_DATE)));

        Field<Integer> open = DSL.count().filterWhere(
                ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()));

        return dslContext
                .select(onTime, late, open)
                .from(ASSIGNED_TASK)
                .where(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(householdMemberId))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus())
                        .or(ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus())
                                .and(ASSIGNED_TASK.COMPLETED_AT.between(from, to))))
                .fetchOne(r -> new CompletionShareResponse(
                        r.get(onTime),
                        r.get(late),
                        r.get(open)
                ));
    }

    public List<CumulatedExperiencePoints> findCumulatedExperiencePoints(UUID householdMemberId, Instant from, Instant to) {
        int startXp = Objects.requireNonNull(dslContext
                        .select(DSL.coalesce(DSL.sum(ASSIGNED_TASK.EARNED_XP), BigDecimal.ZERO))
                        .from(ASSIGNED_TASK)
                        .where(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(householdMemberId))
                        .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus()))
                        .and(ASSIGNED_TASK.COMPLETED_AT.lt(from))
                        .fetchOne(0, BigDecimal.class))
                .intValue();

        Field<BigDecimal> cumulatedXp = DSL.sum(ASSIGNED_TASK.EARNED_XP)
                .over(DSL.orderBy(ASSIGNED_TASK.COMPLETED_AT.asc()));

        return dslContext
                .select(ASSIGNED_TASK.COMPLETED_AT, cumulatedXp)
                .from(ASSIGNED_TASK)
                .where(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(householdMemberId))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.COMPLETED.getStatus()))
                .and(ASSIGNED_TASK.COMPLETED_AT.between(from, to))
                .orderBy(ASSIGNED_TASK.COMPLETED_AT.asc())
                .fetch(r -> new CumulatedExperiencePoints(
                        r.get(ASSIGNED_TASK.COMPLETED_AT),
                        startXp + r.get(cumulatedXp).intValue()
                ));
    }

    public List<HouseholdMemberWorkloadShare> findHouseholdMemberWorkloadShares(UUID householdId, Instant completedFrom) {
        Field<Double> statusFactor = DSL
                .when(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus()),
                        WorkloadFactor.OPEN.getFactor())
                .when(ASSIGNED_TASK.COMPLETED_AT.le(ASSIGNED_TASK.DUE_DATE),
                        WorkloadFactor.COMPLETED_ON_TIME.getFactor())
                .otherwise(WorkloadFactor.COMPLETED_LATE.getFactor());

        Field<BigDecimal> workload = DSL.coalesce(
                DSL.sum(statusFactor.mul(DIFFICULTY.WEIGHT)), BigDecimal.ZERO);

        return dslContext
                .select(USER.USER_NAME, workload)
                .from(HOUSEHOLD_MEMBER)
                .join(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .leftJoin(ASSIGNED_TASK)
                .on(ASSIGNED_TASK.ASSIGNED_MEMBER_ID.eq(HOUSEHOLD_MEMBER.ID))
                .and(ASSIGNED_TASK.STATUS.eq(TaskStatus.OPEN.getStatus())
                        .or(ASSIGNED_TASK.COMPLETED_AT.gt(completedFrom)))
                .leftJoin(TASK).on(TASK.ID.eq(ASSIGNED_TASK.TASK_ID))
                .leftJoin(DIFFICULTY).on(DIFFICULTY.ID.eq(TASK.DIFFICULTY_ID))
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .groupBy(HOUSEHOLD_MEMBER.ID, USER.USER_NAME)
                .orderBy(workload.desc(), USER.USER_NAME.asc())
                .fetch(r -> new HouseholdMemberWorkloadShare(
                        r.get(USER.USER_NAME),
                        r.get(workload).doubleValue()
                ));
    }
}


