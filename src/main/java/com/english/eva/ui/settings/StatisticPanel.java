package com.english.eva.ui.settings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.english.eva.domain.LearningStatus;
import com.english.eva.model.MeaningSearchParams;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import net.miginfocom.swing.MigLayout;

public class StatisticPanel extends JPanel {

    public StatisticPanel(WordService wordService, MeaningService meaningService) {
        setBorder(new TitledBorder("Statistics"));
        setLayout(new MigLayout("fill"));

        var totalWordsCount = String.valueOf(wordService.getAllCount());
        var totalMeaningsCount = String.valueOf(meaningService.getAllCount());
        var totalKnownCount = String.valueOf(meaningService.search(
                new MeaningSearchParams(LearningStatus.KNOWN, null, null)).size());
        var totalLearntCount = String.valueOf(meaningService.search(
                new MeaningSearchParams(LearningStatus.LEARNT, null, null)).size());
        var totalLearningCount = String.valueOf(meaningService.search(
                new MeaningSearchParams(LearningStatus.LEARNING, null, null)).size());
        var totalPutOffCount = String.valueOf(meaningService.search(
                new MeaningSearchParams(LearningStatus.PUT_OFF, null, null)).size());

        add(new JLabel("Total words: "), "cell 0 0");
        add(new JLabel(totalWordsCount), "cell 1 0");
        add(new JLabel("Total meanings: "), "cell 0 1");
        add(new JLabel(totalMeaningsCount), "cell 1 1");

        add(new JLabel("Total known: "), "cell 2 0");
        add(new JLabel(totalKnownCount), "cell 3 0");
        add(new JLabel("Total learnt: "), "cell 2 1");
        add(new JLabel(totalLearntCount), "cell 3 1");

        add(new JLabel("Total learning: "), "cell 4 0");
        add(new JLabel(totalLearningCount), "cell 5 0");
        add(new JLabel("Total put off: "), "cell 4 1");
        add(new JLabel(totalPutOffCount), "cell 5 1");
    }
}
