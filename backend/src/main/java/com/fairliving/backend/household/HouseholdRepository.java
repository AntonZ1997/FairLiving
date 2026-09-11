package com.fairliving.backend.household;

import com.fairliving.backend.household.dto.HouseholdSummaryResponse;
import de.fairliving.backend.jooq.tables.records.HouseholdRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.tables.Household.HOUSEHOLD;
import static de.fairliving.backend.jooq.tables.HouseholdMember.HOUSEHOLD_MEMBER;

@Repository
public class HouseholdRepository {

    private final DSLContext dslContext;

    public HouseholdRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public HouseholdRecord insert(String name, String invitationLink) {
        HouseholdRecord record = dslContext.newRecord(HOUSEHOLD);
        record.setId(UUID.randomUUID());
        record.setName(name);
        record.setInvitationLink(invitationLink);
        record.setCreatedAt(Instant.now());
        record.store();
        return record;
    }

    public Optional<HouseholdRecord> findByInvitationToken(String invitationToken) {
        return dslContext.selectFrom(HOUSEHOLD)
                .where(HOUSEHOLD.INVITATION_LINK.eq(invitationToken))
                .fetchOptionalInto(HouseholdRecord.class);
    }

    public List<HouseholdSummaryResponse> findByUserId(UUID userId) {
        return dslContext
                .select(
                        HOUSEHOLD.ID,
                        HOUSEHOLD.NAME,
                        HOUSEHOLD_MEMBER.ROLE,
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
                        r.get(HOUSEHOLD_MEMBER.JOINED)
                ));
    }
}
