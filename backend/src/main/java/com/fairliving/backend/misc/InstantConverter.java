package com.fairliving.backend.misc;



import jakarta.validation.constraints.NotNull;
import org.jooq.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class InstantConverter implements Converter<LocalDateTime, Instant> {

    @Override
    public Instant from(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(ZoneOffset.UTC);
    }

    @Override
    public LocalDateTime to(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    @Override
    public Class<LocalDateTime>  fromType() {
        return LocalDateTime.class;
    }

    @Override
    public @NotNull Class<Instant> toType() {
        return Instant.class;
    }
}
