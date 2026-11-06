package com.fairliving.backend.exception;

import java.util.UUID;

public class DifficultyNotFoundException extends RuntimeException {
    public DifficultyNotFoundException(UUID difficultyId) {
        super("Difficulty not found: " + difficultyId);
    }
}
