package com.english.eva.model;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;

public class MeaningSearchParams {

    private LearningStatus learningStatus;
    private PartOfSpeech partOfSpeech;
    private ProficiencyLevel proficiencyLevel;

    public MeaningSearchParams() {}

    public MeaningSearchParams(LearningStatus learningStatus, PartOfSpeech partOfSpeech,
                                ProficiencyLevel proficiencyLevel) {
        this.learningStatus = learningStatus;
        this.partOfSpeech = partOfSpeech;
        this.proficiencyLevel = proficiencyLevel;
    }

    public LearningStatus getLearningStatus() { return learningStatus; }
    public void setLearningStatus(LearningStatus learningStatus) { this.learningStatus = learningStatus; }

    public PartOfSpeech getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(PartOfSpeech partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }
}
