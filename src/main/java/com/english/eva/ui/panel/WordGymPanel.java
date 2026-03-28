package com.english.eva.ui.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import com.english.eva.domain.Example;
import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import com.english.eva.model.MeaningSearchParams;
import com.english.eva.service.MeaningService;
import net.miginfocom.swing.MigLayout;

public class WordGymPanel extends JPanel {

    private static final String ANY_OPTION = "Any";

    private final MeaningService meaningService;
    private final Random random = new Random();
    private final Color basicBackground = new JTable().getBackground();
    private final List<Meaning> meanings = new ArrayList<>();
    private final List<Meaning> optionMeanings = new ArrayList<>();
    private final MeaningSearchParams searchParams;
    private final MeaningSearchParams searchParamsForOptionMeanings;
    private final JLabel targetWordMetaData = new JLabel();
    private final JLabel targetWordLabel = new JLabel();
    private final JLabel resultLabel = new JLabel();
    private final JLabel resultCountLabel = new JLabel();
    private final JList<String> options = new JList<>();
    private final JList<String> examplesOfCorrectAnswer = new JList<>();
    private final AtomicInteger meaningIndex = new AtomicInteger(0);

    public WordGymPanel(MeaningService meaningService) {
        this.meaningService = meaningService;
        this.searchParams = new MeaningSearchParams(LearningStatus.LEARNING, null, null);
        this.searchParamsForOptionMeanings = new MeaningSearchParams();
        setLayout(new MigLayout("center"));
        setPreferredSize(new Dimension(600, 600));
        setBackground(basicBackground);
        initComponents();
    }

    private void initComponents() {
        var font = new Font(Font.DIALOG, Font.PLAIN, 20);
        targetWordMetaData.setFont(new Font(font.getName(), Font.PLAIN, font.getSize() - 4));
        targetWordLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 12));
        targetWordLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                var popupMenu = new JPopupMenu();
                Arrays.stream(LearningStatus.values())
                        .map(status -> new JMenuItem(status.getLabel()))
                        .forEach(menuItem -> {
                            menuItem.addActionListener(menuEvent -> {
                                if (!meanings.isEmpty()) {
                                    var currentMeaning = meanings.get(meaningIndex.get());
                                    meaningService.updateLearningStatus(
                                            currentMeaning.getId(),
                                            LearningStatus.findByLabel(menuItem.getText()));
                                }
                            });
                            popupMenu.add(menuItem);
                        });
                popupMenu.show(event.getComponent(), event.getX(), event.getY());
            }
        });

        resultLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 1));
        resultLabel.setText("INIT");
        examplesOfCorrectAnswer.setBorder(new TitledBorder("Examples"));
        examplesOfCorrectAnswer.setVisible(false);
        options.setBorder(new TitledBorder("Options"));

        retrieveMeanings();
        showMeaningWithOptions();

        var prevButton = new JButton("Prev");
        prevButton.addActionListener(e -> loadPrevMeaning());
        var nextButton = new JButton("Next");
        nextButton.addActionListener(e -> loadNextMeaning());
        var submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> handleSubmitAction());

        var settingsPanel = initSettingsPanel();
        var buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("center"));
        buttonPanel.setBackground(basicBackground);
        buttonPanel.add(prevButton, "cell 0 0");
        buttonPanel.add(submitButton, "cell 1 0");
        buttonPanel.add(nextButton, "cell 2 0");

        var meaningPanel = new JPanel();
        meaningPanel.setLayout(new MigLayout("center"));
        meaningPanel.setBackground(basicBackground);
        meaningPanel.add(targetWordMetaData, "cell 0 0, center");
        meaningPanel.add(targetWordLabel, "cell 0 1, center");
        meaningPanel.add(resultLabel, "cell 0 2, center");
        meaningPanel.add(options, "cell 0 3, center");
        meaningPanel.add(buttonPanel, "cell 0 4, center");
        meaningPanel.add(examplesOfCorrectAnswer, "cell 0 5, center");

        add(settingsPanel, "cell 0 0, left, top, gaptop 20, gapleft 20");
        add(meaningPanel, "cell 0 1, push, grow");

        resultLabel.setVisible(false);
    }

    private JPanel initSettingsPanel() {
        var settingsPanel = new JPanel();
        settingsPanel.setLayout(new MigLayout("fill"));
        settingsPanel.setBorder(new TitledBorder("Settings"));
        settingsPanel.setBackground(basicBackground);

        updateFoundedMatchesLabel();

        var partOfSpeechLabel = new JLabel("Part of speech: ");
        var posItems = new ArrayList<String>();
        posItems.add(ANY_OPTION);
        Arrays.stream(PartOfSpeech.values()).map(PartOfSpeech::getLabel).forEach(posItems::add);
        var partOfSpeechOptions = new JComboBox<>(posItems.toArray(new String[0]));
        partOfSpeechOptions.addActionListener(e -> {
            var selectedItem = (String) partOfSpeechOptions.getSelectedItem();
            searchParams.setPartOfSpeech(ANY_OPTION.equals(selectedItem) ? null : PartOfSpeech.findByLabel(selectedItem));
            retrieveMeanings();
            showMeaningWithOptions();
            updateFoundedMatchesLabel();
        });

        var learningStatusLabel = new JLabel("Learning status: ");
        var statusItems = new ArrayList<String>();
        statusItems.add(ANY_OPTION);
        Arrays.stream(LearningStatus.values()).map(LearningStatus::getLabel).forEach(statusItems::add);
        var learningStatusOptions = new JComboBox<>(statusItems.toArray(new String[0]));
        learningStatusOptions.setPreferredSize(
                new Dimension(partOfSpeechOptions.getPreferredSize().width, learningStatusOptions.getPreferredSize().height));
        learningStatusOptions.setSelectedIndex(3); // Learning
        learningStatusOptions.addActionListener(e -> {
            var selectedItem = (String) learningStatusOptions.getSelectedItem();
            searchParams.setLearningStatus(ANY_OPTION.equals(selectedItem) ? null : LearningStatus.findByLabel(selectedItem));
            retrieveMeanings();
            showMeaningWithOptions();
            updateFoundedMatchesLabel();
        });

        var proficiencyLevelLabel = new JLabel("Proficiency level: ");
        var levelItems = new ArrayList<String>();
        levelItems.add(ANY_OPTION);
        Arrays.stream(ProficiencyLevel.values()).map(ProficiencyLevel::toString).forEach(levelItems::add);
        var proficiencyLevelOptions = new JComboBox<>(levelItems.toArray(new String[0]));
        proficiencyLevelOptions.setPreferredSize(
                new Dimension(partOfSpeechOptions.getPreferredSize().width, proficiencyLevelOptions.getPreferredSize().height));
        proficiencyLevelOptions.addActionListener(e -> {
            var selectedItem = (String) proficiencyLevelOptions.getSelectedItem();
            searchParams.setProficiencyLevel(ANY_OPTION.equals(selectedItem) ? null : ProficiencyLevel.valueOf(selectedItem));
            retrieveMeanings();
            showMeaningWithOptions();
            updateFoundedMatchesLabel();
        });

        var refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            retrieveMeanings();
            showMeaningWithOptions();
            updateFoundedMatchesLabel();
        });

        settingsPanel.add(learningStatusLabel, "cell 0 0");
        settingsPanel.add(learningStatusOptions, "cell 1 0");
        settingsPanel.add(partOfSpeechLabel, "cell 0 1");
        settingsPanel.add(partOfSpeechOptions, "cell 1 1");
        settingsPanel.add(proficiencyLevelLabel, "cell 0 2");
        settingsPanel.add(proficiencyLevelOptions, "cell 1 2");
        settingsPanel.add(new JLabel("Founded matches: "), "cell 0 3");
        settingsPanel.add(resultCountLabel, "cell 1 3");
        settingsPanel.add(refreshBtn, "cell 0 4");

        return settingsPanel;
    }

    private void handleSubmitAction() {
        if (meanings.isEmpty()) return;
        var selectedDescription = options.getSelectedValue();
        var currentMeaning = meanings.get(meaningIndex.get());
        if (selectedDescription == null || selectedDescription.isBlank()) return;

        if (currentMeaning.getDescription() != null
                && currentMeaning.getDescription().equals(selectedDescription)) {
            resultLabel.setText("CORRECT");
            resultLabel.setForeground(Color.GREEN);
            var examples = currentMeaning.getExamples().stream()
                    .map(Example::getText).toArray(String[]::new);
            examplesOfCorrectAnswer.setListData(examples);
            examplesOfCorrectAnswer.setVisible(true);
        } else {
            resultLabel.setText("WRONG");
            resultLabel.setForeground(Color.RED);
        }
        resultLabel.setVisible(true);
    }

    private void retrieveMeanings() {
        meanings.clear();
        meanings.addAll(meaningService.search(searchParams));
        Collections.shuffle(meanings);
        meaningIndex.set(0);
    }

    private void loadNextMeaning() {
        if (meaningIndex.get() < meanings.size() - 1) {
            meaningIndex.incrementAndGet();
            updateFoundedMatchesLabel();
            showMeaningWithOptions();
            examplesOfCorrectAnswer.setVisible(false);
        }
    }

    private void loadPrevMeaning() {
        if (meaningIndex.get() > 0) {
            meaningIndex.decrementAndGet();
            updateFoundedMatchesLabel();
            showMeaningWithOptions();
        }
    }

    private void updateFoundedMatchesLabel() {
        resultCountLabel.setText((meaningIndex.get() + 1) + " / " + meanings.size());
    }

    private void retrieveMeaningsForOptions() {
        if (meanings.isEmpty()) return;
        var currentMeaningPartOfSpeech = meanings.get(meaningIndex.get()).getPartOfSpeech();
        if (currentMeaningPartOfSpeech != searchParamsForOptionMeanings.getPartOfSpeech()) {
            optionMeanings.clear();
            searchParamsForOptionMeanings.setPartOfSpeech(currentMeaningPartOfSpeech);
            optionMeanings.addAll(meaningService.search(searchParamsForOptionMeanings));
        }
    }

    private void showMeaningWithOptions() {
        if (meanings.isEmpty()) return;
        var meaning = meanings.get(meaningIndex.get());
        targetWordMetaData.setText(
                "%s / %s / %s".formatted(
                        meaning.getLearningStatus() != null ? meaning.getLearningStatus().getLabel() : "",
                        meaning.getPartOfSpeech() != null ? meaning.getPartOfSpeech().getLabel() : "",
                        meaning.getProficiencyLevel()));
        targetWordLabel.setText(meaning.getTarget());
        resultLabel.setVisible(false);
        retrieveMeaningsForOptions();
        if (optionMeanings.size() < 3) return;
        var listOptions = new ArrayList<String>();
        var randomIndexes = new ArrayList<>(List.of(random.nextInt(optionMeanings.size())));
        for (int i = 1; i <= 2; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(optionMeanings.size());
            } while (randomIndexes.contains(randomIndex));
            randomIndexes.add(randomIndex);
        }
        listOptions.add(meaning.getDescription());
        randomIndexes.forEach(index -> listOptions.add(optionMeanings.get(index).getDescription()));
        Collections.shuffle(listOptions);
        options.setListData(listOptions.toArray(new String[0]));
    }
}
