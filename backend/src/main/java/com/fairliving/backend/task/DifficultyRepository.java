package com.fairliving.backend.task;

import com.fairliving.backend.exception.DifficultyNotFoundException;
import com.fairliving.backend.task.dto.DifficultyResponse;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import static de.fairliving.backend.jooq.Tables.DIFFICULTY;

@Repository
public class DifficultyRepository {

    private final DSLContext dslContext;

    public DifficultyRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<DifficultyResponse> findAll() {
        return dslContext
                .selectFrom(DIFFICULTY)
                .orderBy(DIFFICULTY.WEIGHT.asc())
                .fetch(r -> new DifficultyResponse(
                        r.get(DIFFICULTY.ID),
                        r.get(DIFFICULTY.NAME),
                        r.get(DIFFICULTY.BASE_XP),
                        r.get(DIFFICULTY.WEIGHT)
                ));
    }

    public boolean exists(UUID difficultyId) {
        return dslContext.fetchExists(
                dslContext.selectFrom(DIFFICULTY).where(DIFFICULTY.ID.eq(difficultyId))
        );
    }

    public int findBaseXp(UUID difficultyId) {
        return dslContext
                .select(DIFFICULTY.BASE_XP)
                .from(DIFFICULTY)
                .where(DIFFICULTY.ID.eq(difficultyId))
                .fetchOptional(r -> r.get(DIFFICULTY.BASE_XP))
                .orElseThrow(() -> new DifficultyNotFoundException(difficultyId));
    }

}
