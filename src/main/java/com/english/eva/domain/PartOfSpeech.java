package com.english.eva.domain;

import java.util.Arrays;

public enum PartOfSpeech {
    ADJECTIVE("Adjective"),
    ADVERB("Adverb"),
    AUXILIARY_VERB("Auxiliary verb"),
    CONJUNCTION("Conjunction"),
    DETERMINER("Determiner"),
    EXCLAMATION("Exclamation"),
    MODAL_VERB("Modal verb"),
    NOUN("Noun"),
    PHRASAL_VERB("Phrasal verb"),
    PHRASE("Phrase"),
    PREPOSITION("Preposition"),
    PRONOUN("Pronoun"),
    VERB("Verb");

    private final String label;

    PartOfSpeech(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static PartOfSpeech findByLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }
}
