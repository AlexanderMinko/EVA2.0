package com.english.eva.ui.word;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.Word;

public class WordDto {

    private final long id;
    private final String text;
    private final String transcript;
    private final String frequency;
    private final String progress;
    private final String levels;
    private final String partsOfSpeech;
    private final List<Long> meaningIds;

    public WordDto(Word word) {
        this.id = word.getId();
        this.text = word.getText();
        this.transcript = word.getTranscript() != null ? "[ " + word.getTranscript() + " ]" : "";
        this.frequency = String.valueOf(word.getFrequency());
        this.progress = computeProgress(word.getMeanings());
        this.levels = computeLevels(word.getMeanings());
        this.partsOfSpeech = computePartsOfSpeech(word.getMeanings());
        this.meaningIds = word.getMeanings().stream().map(Meaning::getId).toList();
    }

    private static String computeProgress(List<Meaning> meanings) {
        if (meanings.isEmpty()) return "0";
        long knownCount = meanings.stream()
                .filter(m -> m.getLearningStatus() == LearningStatus.KNOWN
                        || m.getLearningStatus() == LearningStatus.LEARNT)
                .count();
        return String.valueOf((int) (((double) knownCount / meanings.size()) * 100));
    }

    private static String computeLevels(List<Meaning> meanings) {
        return meanings.stream()
                .map(Meaning::getProficiencyLevel)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(" "));
    }

    private static String computePartsOfSpeech(List<Meaning> meanings) {
        return meanings.stream()
                .map(Meaning::getPartOfSpeech)
                .filter(Objects::nonNull)
                .map(PartOfSpeech::getLabel)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    public long getId() { return id; }
    public String getText() { return text; }
    public String getTranscript() { return transcript; }
    public String getFrequency() { return frequency; }
    public String getProgress() { return progress; }
    public String getLevels() { return levels; }
    public String getPartsOfSpeech() { return partsOfSpeech; }
    public List<Long> getMeaningIds() { return meaningIds; }
}
