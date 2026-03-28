package com.english.eva.model;

import java.util.Set;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.ProficiencyLevel;

public record SearchParams(
        String text,
        Set<ProficiencyLevel> levels,
        Set<LearningStatus> statuses
) {
    public SearchParams {
        if (levels == null) levels = Set.of();
        if (statuses == null) statuses = Set.of();
    }
}
