package com.english.eva.ui.meaning;

import java.util.Objects;

import com.english.eva.domain.Meaning;
import lombok.Getter;

@Getter
public class MeaningDto {

    private final long id;
    private final String target;
    private final String level;
    private final String status;
    private final String partOfSpeech;

    public MeaningDto(Meaning meaning) {
        this.id = meaning.getId();
        this.target = meaning.getTarget();
        this.level = Objects.nonNull(meaning.getProficiencyLevel())
                ? meaning.getProficiencyLevel().name() : "";
        this.status = Objects.nonNull(meaning.getLearningStatus())
                ? meaning.getLearningStatus().getLabel() : "";
        this.partOfSpeech = Objects.nonNull(meaning.getPartOfSpeech())
                ? meaning.getPartOfSpeech().getLabel() : "";
    }
}
