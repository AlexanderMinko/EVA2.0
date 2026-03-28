package com.english.eva.model;

import java.util.Objects;
import java.util.Set;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.ProficiencyLevel;

public record SearchParams(
        String text,
        Set<ProficiencyLevel> levels,
        Set<LearningStatus> statuses
) {
    public SearchParams {
        if (Objects.isNull(levels)) levels = Set.of();
        if (Objects.isNull(statuses)) statuses = Set.of();
    }
}
