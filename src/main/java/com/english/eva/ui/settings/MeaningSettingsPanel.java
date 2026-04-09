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
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import com.english.eva.model.MeaningSearchParams;

public class MeaningSettingsPanel extends JPanel {

    private final Consumer<MeaningSearchParams> onSearch;
    private JTextField searchField;
    private JButton searchButton;
    private List<JToggleButton> levelButtons;
    private List<JToggleButton> statusButtons;
    private JComboBox<String> partOfSpeechCombo;
    private JPanel levelsPanel;
    private JPanel statusPanel;
    private JPanel posPanel;

    public MeaningSettingsPanel(Consumer<MeaningSearchParams> onSearch) {
        this.onSearch = onSearch;

        setBorder(new TitledBorder("Settings"));
        var groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setAutoCreateContainerGaps(true);
        setLayout(groupLayout);

        initSearchField();
        initSearchButton();
        initLevelsBar();
        initStatusBar();
        initPartOfSpeechCombo();

        var levelLabel = new JLabel("Level:");
        var statusLabel = new JLabel("Status:");
        var posLabel = new JLabel("Part of Speech:");

        groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(LEADING)
                        .addComponent(searchField)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(levelLabel)
                                .addComponent(levelsPanel)))
                .addGroup(groupLayout.createParallelGroup(LEADING)
                        .addComponent(searchButton)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addComponent(statusLabel)
                                .addComponent(statusPanel)
                                .addComponent(posLabel)
                                .addComponent(posPanel))));

        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup(BASELINE)
                        .addComponent(searchField)
                        .addComponent(searchButton))
                .addGroup(groupLayout.createParallelGroup(BASELINE)
                        .addComponent(levelLabel)
                        .addComponent(levelsPanel)
                        .addComponent(statusLabel)
                        .addComponent(statusPanel)
                        .addComponent(posLabel)
                        .addComponent(posPanel)));
    }

    public void triggerSearch() {
        executeSearch();
    }

    private void initSearchField() {
        searchField = new JTextField("Search...", 20);
        searchField.setForeground(Color.GRAY);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSearch();
                }
            }
        });
        searchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if ("Search...".equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Search...");
                }
            }
        });
    }

    private void initSearchButton() {
        searchButton = new JButton("Search");
        searchButton.addActionListener(event -> executeSearch());
    }

    private void initLevelsBar() {
        levelButtons = List.of(
                new JToggleButton(ProficiencyLevel.A1.name()),
                new JToggleButton(ProficiencyLevel.A2.name()),
                new JToggleButton(ProficiencyLevel.B1.name()),
                new JToggleButton(ProficiencyLevel.B2.name()),
                new JToggleButton(ProficiencyLevel.C1.name()),
                new JToggleButton(ProficiencyLevel.C2.name()),
                new JToggleButton(ProficiencyLevel.J7.name())
        );
        var toggleAll = new JToggleButton("Select All");

        levelButtons.forEach(btn -> {
            btn.setForeground(Color.WHITE);
            btn.setBackground(LEVEL_COLOURS.get(btn.getText()));
            var font = btn.getFont();
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
            levelButtons.forEach(btn -> {
                btn.setSelected(selected);
                btn.setBackground(btn.isSelected()
                        ? new Color(0, 0, 0, 0)
                        : LEVEL_COLOURS.get(btn.getText()));
            });
        });

        levelsPanel = new JPanel();
        levelsPanel.setLayout(new BoxLayout(levelsPanel, BoxLayout.X_AXIS));
        levelButtons.forEach(levelsPanel::add);
        levelsPanel.add(toggleAll);
    }

    private void initStatusBar() {
        statusButtons = List.of(
                new JToggleButton(LearningStatus.KNOWN.getLabel()),
                new JToggleButton(LearningStatus.LEARNT.getLabel()),
                new JToggleButton(LearningStatus.LEARNING.getLabel()),
                new JToggleButton(LearningStatus.PUT_OFF.getLabel()),
                new JToggleButton(LearningStatus.UNDEFINED.getLabel())
        );
        var toggleAll = new JToggleButton("Select All");

        statusButtons.forEach(toggle -> {
            toggle.setBackground(LEARNING_COLOURS.get(toggle.getText()));
            var font = toggle.getFont();
            toggle.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            toggle.addActionListener(event ->
                    toggle.setBackground(toggle.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(toggle.getText())));
        });
        toggleAll.addActionListener(event ->
                statusButtons.forEach(btn -> {
                    btn.setSelected(toggleAll.isSelected());
                    btn.setBackground(btn.isSelected()
                            ? new Color(0, 0, 0, 0)
                            : LEARNING_COLOURS.get(btn.getText()));
                }));

        statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusButtons.forEach(statusPanel::add);
        statusPanel.add(toggleAll);
    }

    private void initPartOfSpeechCombo() {
        var items = new String[PartOfSpeech.values().length + 1];
        items[0] = "All";
        for (int i = 0; i < PartOfSpeech.values().length; i++) {
            items[i + 1] = PartOfSpeech.values()[i].getLabel();
        }
        partOfSpeechCombo = new JComboBox<>(new DefaultComboBoxModel<>(items));

        posPanel = new JPanel();
        posPanel.setLayout(new BoxLayout(posPanel, BoxLayout.X_AXIS));
        posPanel.add(partOfSpeechCombo);
    }

    private void executeSearch() {
        var searchText = searchField.getText();
        String text = "Search...".equals(searchText) ? null : searchText;

        Set<ProficiencyLevel> selectedLevels = levelButtons.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(ProficiencyLevel::valueOf)
                .collect(Collectors.toSet());

        Set<LearningStatus> selectedStatuses = statusButtons.stream()
                .filter(AbstractButton::isSelected)
                .map(AbstractButton::getText)
                .map(LearningStatus::findByLabel)
                .collect(Collectors.toSet());

        var selectedPos = (String) partOfSpeechCombo.getSelectedItem();
        PartOfSpeech partOfSpeech = "All".equals(selectedPos) ? null : PartOfSpeech.findByLabel(selectedPos);

        onSearch.accept(new MeaningSearchParams(text, selectedLevels, selectedStatuses, partOfSpeech));
    }
}
