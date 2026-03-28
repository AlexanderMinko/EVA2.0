package com.english.eva.domain;

import java.util.Arrays;

public enum LearningStatus {
    KNOWN("Known"),
    LEARNT("Learnt"),
    LEARNING("Learning"),
    PUT_OFF("Put off"),
    UNDEFINED("Undefined");

    private final String label;

    LearningStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static LearningStatus findByLabel(String label) {
        return Arrays.stream(values())
                .filter(v -> v.label.equalsIgnoreCase(label))
                .findFirst()
                .orElse(null);
    }
}
