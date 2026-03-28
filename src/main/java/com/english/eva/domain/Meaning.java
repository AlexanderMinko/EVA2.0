package com.english.eva.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Meaning {

    private Long id;
    private Long wordId;
    private String target;
    private String transcript;
    private String topic;
    private String description;
    private PartOfSpeech partOfSpeech;
    private ProficiencyLevel proficiencyLevel;
    private MeaningSource meaningSource;
    private LearningStatus learningStatus;
    private LocalDateTime dateCreated;
    private LocalDateTime lastModified;
    private List<Example> examples = new ArrayList<>();

    public Meaning(Long id, Long wordId, String target, String transcript, String topic,
                   String description, PartOfSpeech partOfSpeech, ProficiencyLevel proficiencyLevel,
                   MeaningSource meaningSource, LearningStatus learningStatus,
                   LocalDateTime dateCreated, LocalDateTime lastModified) {
        this.id = id;
        this.wordId = wordId;
        this.target = target;
        this.transcript = transcript;
        this.topic = topic;
        this.description = description;
        this.partOfSpeech = partOfSpeech;
        this.proficiencyLevel = proficiencyLevel;
        this.meaningSource = meaningSource;
        this.learningStatus = learningStatus;
        this.dateCreated = dateCreated;
        this.lastModified = lastModified;
    }
}
