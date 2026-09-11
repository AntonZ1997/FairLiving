package com.fairliving.backend.gamification;

import de.fairliving.backend.jooq.tables.records.LevelRecord;
import org.springframework.stereotype.Service;

@Service
public class GamificationService {

    private final LevelRepository levelRepository;

    public GamificationService(LevelRepository levelRepository) {
        this.levelRepository = levelRepository;
    }

    public LevelRecord resolveLevel(int experiencePoints) {
        return levelRepository.findByXp(experiencePoints)
                .orElseThrow(() -> new IllegalStateException(
                        "No level defined for XP: "  + experiencePoints
                ));
    }
}
