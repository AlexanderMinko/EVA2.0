package com.english.eva.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MeaningSource {
    ENGLISH_PROFILE("English Profile"),
    CAMBRIDGE_DICTIONARY("Cambridge dictionary"),
    GOOGLE("Google");

    private final String label;

    MeaningSource(String label) {
        this.label = label;
    }

    public static MeaningSource findByLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }
}
