package com.english.eva.ui.settings;

import static com.english.eva.ui.util.ColorUtils.LEARNING_COLOURS;
import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;
import static javax.swing.GroupLayout.Alignment.BASELINE;
import static javax.swing.GroupLayout.Alignment.LEADING;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.ProficiencyLevel;
import com.english.eva.model.SearchParams;

public class SettingsPanel extends JPanel {

    private final Consumer<SearchParams> onSearch;
    private JTextField wordSearchValueField;
    private List<JToggleButton> levelButtonsList;
    private List<JToggleButton> learningStatusButtonList;
    private JPanel levelsPanel;
    private JPanel learningStatusPanel;
    private JButton searchButton;

    public SettingsPanel(Consumer<SearchParams> onSearch) {
        this.onSearch = onSearch;

        setBorder(new TitledBorder("Settings"));
        var groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        setLayout(groupLayout);

        initWordSearchValueField();
        initSearchButton();
        initLevelsBar();
        initLearningStatusBar();

        groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(LEADING)
                        .addComponent(wordSearchValueField).addComponent(levelsPanel))
                .addGroup(groupLayout.createParallelGroup(LEADING)
                        .addComponent(searchButton).addComponent(learningStatusPanel)));

        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(BASELINE)
                        .addComponent(wordSearchValueField).addComponent(searchButton))
                .addGroup(groupLayout.createParallelGroup(BASELINE)
                        .addComponent(levelsPanel).addComponent(learningStatusPanel)));
    }

    private void initLearningStatusBar() {
        var toggleKnown = new JToggleButton(LearningStatus.KNOWN.getLabel());
        var toggleLearnt = new JToggleButton(LearningStatus.LEARNT.getLabel());
        var toggleLearning = new JToggleButton(LearningStatus.LEARNING.getLabel());
        var togglePutOff = new JToggleButton(LearningStatus.PUT_OFF.getLabel());
        var toggleUndefined = new JToggleButton(LearningStatus.UNDEFINED.getLabel());
        var toggleAll = new JToggleButton("Select All");

        learningStatusButtonList = List.of(toggleKnown, toggleLearnt, toggleLearning, togglePutOff, toggleUndefined);
        learningStatusButtonList.forEach(toggle -> {
            toggle.setBackground(LEARNING_COLOURS.get(toggle.getText()));
            var font = toggle.getFont();
            toggle.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            toggle.addActionListener(event ->
                    toggle.setBackground(toggle.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(toggle.getText())));
        });
        toggleAll.addActionListener(event ->
                learningStatusButtonList.forEach(btn -> {
                    btn.setSelected(toggleAll.isSelected());
                    btn.setBackground(btn.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(btn.getText()));
                }));

        learningStatusPanel = new JPanel();
        learningStatusPanel.setLayout(new BoxLayout(learningStatusPanel, BoxLayout.X_AXIS));
        learningStatusButtonList.forEach(learningStatusPanel::add);
        learningStatusPanel.add(toggleAll);
    }

    private void initSearchButton() {
        searchButton = new JButton("Search");
        searchButton.addActionListener(event -> executeWordSearch());
    }

    private void initLevelsBar() {
        var toggleA1 = new JToggleButton(ProficiencyLevel.A1.name());
        var toggleA2 = new JToggleButton(ProficiencyLevel.A2.name());
        var toggleB1 = new JToggleButton(ProficiencyLevel.B1.name());
        var toggleB2 = new JToggleButton(ProficiencyLevel.B2.name());
        var toggleC1 = new JToggleButton(ProficiencyLevel.C1.name());
        var toggleC2 = new JToggleButton(ProficiencyLevel.C2.name());
        var toggleJ7 = new JToggleButton(ProficiencyLevel.J7.name());
        var toggleAll = new JToggleButton("Select All");

        levelButtonsList = List.of(toggleA1, toggleA2, toggleB1, toggleB2, toggleC1, toggleC2, toggleJ7);
        levelButtonsList.forEach(btn -> {
            btn.setForeground(Color.WHITE);
            var font = btn.getFont();
            btn.setBackground(LEVEL_COLOURS.get(btn.getText()));
            btn.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            btn.addActionListener(e -> {
                btn.setForeground(Color.WHITE);
                btn.setBackground(btn.isSelected()
                        ? new Color(0, 0, 0, 0)
                        : LEVEL_COLOURS.get(btn.getText()));
            });
        });

        toggleAll.addActionListener(event -> {
            var selected = ((AbstractButton) event.getSource()).getModel().isSelected();
            levelButtonsList.forEach(btn -> {
                btn.setSelected(selected);
                btn.setBackground(btn.isSelected()
                        ? new Color(0, 0, 0, 0)
                        : LEVEL_COLOURS.get(btn.getText()));
            });
        });

        levelsPanel = new JPanel();
        levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.X_AXIS));
        levelButtonsList.forEach(levelsPanel::add);
        levelsPanel.add(toggleAll);
    }

    private void initWordSearchValueField() {
        wordSearchValueField = new JTextField("Search...", 20);
        wordSearchValueField.setForeground(Color.GRAY);
        wordSearchValueField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeWordSearch();
                }
            }
        });
        wordSearchValueField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if ("Search...".equals(wordSearchValueField.getText())) {
                    wordSearchValueField.setText("");
                    wordSearchValueField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (wordSearchValueField.getText().isEmpty()) {
                    wordSearchValueField.setForeground(Color.GRAY);
                    wordSearchValueField.setText("Search...");
                }
            }
        });
    }

    private void executeWordSearch() {
        var searchText = wordSearchValueField.getText();
        String text = "Search...".equals(searchText) ? null : searchText;

        Set<ProficiencyLevel> selectedLevels = levelButtonsList.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(ProficiencyLevel::valueOf)
                .collect(Collectors.toSet());

        Set<LearningStatus> selectedStatuses = learningStatusButtonList.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(LearningStatus::findByLabel)
                .collect(Collectors.toSet());

        onSearch.accept(new SearchParams(text, selectedLevels, selectedStatuses));
    }
}
