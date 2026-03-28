package com.english.eva.domain;

import java.util.Arrays;

public enum MeaningSource {
    ENGLISH_PROFILE("English Profile"),
    CAMBRIDGE_DICTIONARY("Cambridge dictionary"),
    GOOGLE("Google");

    private final String label;

    MeaningSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static MeaningSource findByLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }
}
