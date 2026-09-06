package com.fairliving.backend.user;

import de.fairliving.backend.jooq.tables.records.UserRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static de.fairliving.backend.jooq.Tables.USER;

@Repository
public class UserRepository {
    private final DSLContext dslContext;

    public UserRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public Optional<UserRecord> findByEmail(String email) {
        return dslContext
                .selectFrom(USER)
                .where(USER.EMAIL.eq(email))
                .fetchOptionalInto(UserRecord.class);
    }

    public boolean existsByEmail(String email) {
        return dslContext
                .fetchExists(
                        dslContext.selectFrom(USER)
                                .where(USER.EMAIL.eq(email))
                );
    }

    public UserRecord insert(String email, String passwordHash, String userName) {
        UserRecord userRecord = dslContext.newRecord(USER);
        userRecord.setId(UUID.randomUUID());
        userRecord.setEmail(email);
        userRecord.setPassword(passwordHash);
        userRecord.setUserName(userName);
        userRecord.setCreatedAt(Instant.now());
        userRecord.store();
        return userRecord;
    }
}
