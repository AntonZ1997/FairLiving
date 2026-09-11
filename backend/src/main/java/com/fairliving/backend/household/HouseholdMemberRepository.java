package com.fairliving.backend.household;

import com.fairliving.backend.household.dto.HouseholdMemberResponse;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.HouseholdMember.HOUSEHOLD_MEMBER;
import static de.fairliving.backend.jooq.tables.Level.LEVEL;
import static de.fairliving.backend.jooq.tables.User.USER;

@Repository
public class HouseholdMemberRepository {
    private final DSLContext dslContext;

    public HouseholdMemberRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void insert(UUID householdId, UUID userId, HouseholdRole role, UUID levelId, int experiencePoints) {
        HouseholdMemberRecord householdMember = dslContext.newRecord(HOUSEHOLD_MEMBER);
        householdMember.setId(UUID.randomUUID());
        householdMember.setHouseholdId(householdId);
        householdMember.setUserId(userId);
        householdMember.setRole(role.getRole());
        householdMember.setStreakCount(0);
        householdMember.setExperiencePoints(experiencePoints);
        householdMember.setLevelId(levelId);
        householdMember.setJoined(Instant.now());
        householdMember.store();
    }

    public boolean exists(UUID householdId, UUID userId) {
        return dslContext.fetchExists(
                dslContext.selectFrom(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId)
                        .and(HOUSEHOLD_MEMBER.USER_ID.eq(userId))));
    }

    public List<HouseholdMemberResponse> findMembers(UUID householdId) {
        return dslContext
                .select(
                        HOUSEHOLD_MEMBER.ID,
                        USER.USER_NAME,
                        HOUSEHOLD_MEMBER.ROLE,
                        HOUSEHOLD_MEMBER.EXPERIENCE_POINTS,
                        HOUSEHOLD_MEMBER.STREAK_COUNT,
                        LEVEL.LEVEL_NUMBER,
                        LEVEL.TITLE,
                        HOUSEHOLD_MEMBER.JOINED
                ).from(HOUSEHOLD_MEMBER)
                .join(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .join(LEVEL).on(LEVEL.ID.eq(HOUSEHOLD_MEMBER.LEVEL_ID))
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .orderBy(HOUSEHOLD_MEMBER.EXPERIENCE_POINTS.desc())
                .fetch(r ->
                        new HouseholdMemberResponse(
                                r.get(HOUSEHOLD_MEMBER.ID),
                                r.get(USER.USER_NAME),
                                HouseholdRole.fromString(r.get(HOUSEHOLD_MEMBER.ROLE)),
                                r.get(HOUSEHOLD_MEMBER.EXPERIENCE_POINTS),
                                r.get(HOUSEHOLD_MEMBER.STREAK_COUNT),
                                r.get(LEVEL.LEVEL_NUMBER),
                                r.get(LEVEL.TITLE),
                                r.get(HOUSEHOLD_MEMBER.JOINED)
                        ));
    }

    public Optional<HouseholdMemberRecord> findMembership(UUID householdId, UUID userId) {
        return dslContext
                .selectFrom(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .and(HOUSEHOLD_MEMBER.USER_ID.eq(userId))
                .fetchOptionalInto(HouseholdMemberRecord.class);
    }

}
