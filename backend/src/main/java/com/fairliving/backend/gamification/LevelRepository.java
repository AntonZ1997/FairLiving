package com.fairliving.backend.gamification;

import de.fairliving.backend.jooq.tables.records.LevelRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static de.fairliving.backend.jooq.tables.Level.LEVEL;

@Repository
public class LevelRepository {
    private final DSLContext dslContext;

    public LevelRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Optional<LevelRecord> findByXp(int experiencePoints) {
        return dslContext
                .selectFrom(LEVEL)
                .where(LEVEL.REQUIRED_XP.le(experiencePoints))
                .orderBy(LEVEL.REQUIRED_XP.desc())
                .limit(1)
                .fetchOptionalInto(LevelRecord.class);
    }
}
