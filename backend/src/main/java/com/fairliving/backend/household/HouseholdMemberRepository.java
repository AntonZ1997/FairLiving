package com.fairliving.backend.household;

import com.fairliving.backend.household.dto.HouseholdMemberResponse;
import de.fairliving.backend.jooq.tables.Level;
import de.fairliving.backend.jooq.tables.records.HouseholdMemberRecord;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
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

    public UUID insert(UUID householdId, UUID userId, HouseholdRole role, UUID levelId, int experiencePoints) {
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
        return householdMember.getId();
    }

    public boolean exists(UUID householdId, UUID userId) {
        return dslContext.fetchExists(
                dslContext.selectFrom(HOUSEHOLD_MEMBER)
                        .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId)
                                .and(HOUSEHOLD_MEMBER.USER_ID.eq(userId))));
    }

    public Optional<HouseholdMemberRecord> findById(UUID memberId) {
        return dslContext
                .selectFrom(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.ID.eq(memberId))
                .fetchOptionalInto(HouseholdMemberRecord.class);
    }

    public void updateProgress(UUID memberId, int experiencePoints, int streakCount, UUID levelId) {
        dslContext
                .update(HOUSEHOLD_MEMBER)
                .set(HOUSEHOLD_MEMBER.EXPERIENCE_POINTS, experiencePoints)
                .set(HOUSEHOLD_MEMBER.STREAK_COUNT, streakCount)
                .set(HOUSEHOLD_MEMBER.LEVEL_ID, levelId)
                .where(HOUSEHOLD_MEMBER.ID.eq(memberId))
                .execute();
    }

    public List<HouseholdMemberResponse> findMembers(UUID householdId, UUID currentUserId) {
        Level nextLevel = LEVEL.as("next_level");

        Field<Boolean> isCurrentUser = DSL.field(HOUSEHOLD_MEMBER.USER_ID.eq(currentUserId)).as("is_current_user");

        Field<Integer> nextLevelRequiredXp = dslContext
                .select(DSL.min(nextLevel.REQUIRED_XP))
                .from(nextLevel)
                .where(nextLevel.REQUIRED_XP.gt(LEVEL.REQUIRED_XP))
                .asField("next_level_required_xp");

        return dslContext
                .select(
                        HOUSEHOLD_MEMBER.ID,
                        USER.USER_NAME,
                        HOUSEHOLD_MEMBER.ROLE,
                        HOUSEHOLD_MEMBER.EXPERIENCE_POINTS,
                        HOUSEHOLD_MEMBER.STREAK_COUNT,
                        LEVEL.LEVEL_NUMBER,
                        LEVEL.TITLE,
                        LEVEL.REQUIRED_XP,
                        nextLevelRequiredXp,
                        isCurrentUser,
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
                                r.get(LEVEL.REQUIRED_XP),
                                r.get(nextLevelRequiredXp),
                                r.get(isCurrentUser),
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

    public int countMembersByHouseholdId(UUID householdId) {
        return dslContext
                .fetchCount(
                        dslContext.selectFrom(HOUSEHOLD_MEMBER)
                                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                );
    }

    public List<UUID> findHouseholdMemberIdsByHouseholdId(UUID householdId) {
        return dslContext
                .select(HOUSEHOLD_MEMBER.ID)
                .from(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .fetch(r -> r.get(HOUSEHOLD_MEMBER.ID));
    }

    public Optional<String> findUserNameByMemberId(UUID householdMemberId) {
        return dslContext
                .select(USER.USER_NAME)
                .from(HOUSEHOLD_MEMBER)
                .join(USER).on(USER.ID.eq(HOUSEHOLD_MEMBER.USER_ID))
                .where(HOUSEHOLD_MEMBER.ID.eq(householdMemberId))
                .fetchOptional(r -> r.get(USER.USER_NAME));
    }

    public void delete(UUID memberId) {
        dslContext
                .deleteFrom(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.ID.eq(memberId))
                .execute();
    }

    public void updateRole(UUID memberId, HouseholdRole role) {
        dslContext
                .update(HOUSEHOLD_MEMBER)
                .set(HOUSEHOLD_MEMBER.ROLE, role.getRole())
                .where(HOUSEHOLD_MEMBER.ID.eq(memberId))
                .execute();
    }

    public Optional<UUID> findOldestMember(UUID householdId, UUID exceptionMemberId) {
        return dslContext
                .select(HOUSEHOLD_MEMBER.ID)
                .from(HOUSEHOLD_MEMBER)
                .where(HOUSEHOLD_MEMBER.HOUSEHOLD_ID.eq(householdId))
                .and(HOUSEHOLD_MEMBER.ID.ne(exceptionMemberId))
                .orderBy(HOUSEHOLD_MEMBER.JOINED.asc())
                .limit(1)
                .fetchOptional(r -> r.get(HOUSEHOLD_MEMBER.ID));
    }

}
