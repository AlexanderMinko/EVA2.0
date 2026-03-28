package com.english.eva.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Meaning() {}

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWordId() { return wordId; }
    public void setWordId(Long wordId) { this.wordId = wordId; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PartOfSpeech getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(PartOfSpeech partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    public MeaningSource getMeaningSource() { return meaningSource; }
    public void setMeaningSource(MeaningSource meaningSource) { this.meaningSource = meaningSource; }

    public LearningStatus getLearningStatus() { return learningStatus; }
    public void setLearningStatus(LearningStatus learningStatus) { this.learningStatus = learningStatus; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public List<Example> getExamples() { return examples; }
    public void setExamples(List<Example> examples) { this.examples = examples; }
}
