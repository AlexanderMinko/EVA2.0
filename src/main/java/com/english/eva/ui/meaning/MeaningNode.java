package com.english.eva.ui.meaning;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;

public class MeaningNode {

    public enum NodeType {
        SOURCE, PART_OF_SPEECH, MEANING, DESCRIPTION, EXAMPLES_HEADER, EXAMPLE
    }

    private final NodeType type;
    private final String displayText;
    private Long meaningId;
    private LearningStatus learningStatus;
    private PartOfSpeech partOfSpeech;
    private ProficiencyLevel proficiencyLevel;
    private String description;

    public MeaningNode(NodeType type, String displayText) {
        this.type = type;
        this.displayText = displayText;
    }

    public static MeaningNode source(String label) {
        return new MeaningNode(NodeType.SOURCE, label);
    }

    public static MeaningNode partOfSpeech(String label) {
        return new MeaningNode(NodeType.PART_OF_SPEECH, label);
    }

    public static MeaningNode meaning(Long meaningId, String target, LearningStatus status) {
        var node = new MeaningNode(NodeType.MEANING, target);
        node.meaningId = meaningId;
        node.learningStatus = status;
        return node;
    }

    public static MeaningNode description(ProficiencyLevel level, String description) {
        var node = new MeaningNode(NodeType.DESCRIPTION, description);
        node.proficiencyLevel = level;
        node.description = description;
        return node;
    }

    public static MeaningNode examplesHeader() {
        return new MeaningNode(NodeType.EXAMPLES_HEADER, "Examples");
    }

    public static MeaningNode example(String text) {
        return new MeaningNode(NodeType.EXAMPLE, text);
    }

    public NodeType getType() { return type; }
    public String getDisplayText() { return displayText; }
    public Long getMeaningId() { return meaningId; }
    public LearningStatus getLearningStatus() { return learningStatus; }
    public PartOfSpeech getPartOfSpeech() { return partOfSpeech; }
    public ProficiencyLevel getProficiencyLevel() { return proficiencyLevel; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return displayText;
    }
}
