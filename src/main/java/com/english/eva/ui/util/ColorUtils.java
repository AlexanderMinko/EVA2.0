package com.english.eva.ui.util;

import java.awt.Color;
import java.util.Map;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.ProficiencyLevel;

public final class ColorUtils {

    private ColorUtils() {}

    public static final Map<String, Color> LEVEL_COLOURS = Map.of(
            ProficiencyLevel.A1.name(), new Color(255, 128, 0),
            ProficiencyLevel.A2.name(), new Color(0, 160, 160),
            ProficiencyLevel.B1.name(), new Color(255, 0, 0),
            ProficiencyLevel.B2.name(), new Color(0, 128, 64),
            ProficiencyLevel.C1.name(), new Color(48, 96, 255),
            ProficiencyLevel.C2.name(), new Color(160, 48, 160),
            ProficiencyLevel.J7.name(), new Color(26, 27, 31)
    );

    public static final Map<String, Color> LEARNING_COLOURS = Map.of(
            LearningStatus.KNOWN.getLabel(), new Color(156, 255, 205),
            LearningStatus.LEARNT.getLabel(), new Color(85, 173, 129),
            LearningStatus.LEARNING.getLabel(), new Color(255, 189, 128),
            LearningStatus.PUT_OFF.getLabel(), new Color(184, 148, 197),
            LearningStatus.UNDEFINED.getLabel(), new Color(232, 218, 237)
    );
}
