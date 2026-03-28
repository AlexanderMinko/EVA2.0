package com.english.eva.model;

import java.util.Set;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeaningSearchParams {

    private String text;
    private Set<ProficiencyLevel> proficiencyLevels;
    private Set<LearningStatus> learningStatuses;
    private PartOfSpeech partOfSpeech;

    /**
     * Backward-compatible constructor used by WordGymPanel and StatisticPanel.
     */
    public MeaningSearchParams(LearningStatus learningStatus, PartOfSpeech partOfSpeech,
                                ProficiencyLevel proficiencyLevel) {
        this.learningStatuses = learningStatus != null ? Set.of(learningStatus) : Set.of();
        this.partOfSpeech = partOfSpeech;
        this.proficiencyLevels = proficiencyLevel != null ? Set.of(proficiencyLevel) : Set.of();
    }
}
