package com.english.eva.ui.word;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.english.eva.domain.Example;
import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.MeaningSource;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.domain.ProficiencyLevel;
import com.english.eva.domain.Word;
import com.english.eva.repository.ExampleRepository;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import com.english.eva.ui.meaning.MeaningTree;
import com.english.eva.ui.util.UiUtils;
import net.miginfocom.swing.MigLayout;

public class TableClickListener extends MouseAdapter {

    private final WordsTable wordsTable;
    private final MeaningTree meaningTree;
    private final WordService wordService;
    private final MeaningService meaningService;
    private final ExampleRepository exampleRepository;

    private int formRow = -1;

    public TableClickListener(WordsTable wordsTable, MeaningTree meaningTree,
                               WordService wordService, MeaningService meaningService,
                               ExampleRepository exampleRepository) {
        this.wordsTable = wordsTable;
        this.meaningTree = meaningTree;
        this.wordService = wordService;
        this.meaningService = meaningService;
        this.exampleRepository = exampleRepository;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        var point = event.getPoint();
        var currentRow = wordsTable.rowAtPoint(point);
        wordsTable.setRowSelectionInterval(currentRow, currentRow);
        var wordDto = wordsTable.getWordTableModel().getWordDtoAt(currentRow);
        var selectedWordId = wordDto.getId();

        if (SwingUtilities.isRightMouseButton(event)) {
            showWordPopupMenu(event, selectedWordId);
        }
        if (SwingUtilities.isLeftMouseButton(event)) {
            UiUtils.runInBackground(
                    () -> wordService.getByIdWithMeanings(selectedWordId),
                    result -> result.ifPresent(word -> {
                        meaningTree.setWord(word);
                        meaningTree.showSelectedUserObjectTree();
                    })
            );
        }
    }

    private void showWordPopupMenu(MouseEvent e, long selectedWordId) {
        var popupMenu = new JPopupMenu();

        var addNewWordItem = new JMenuItem("Add new word");
        addNewWordItem.addActionListener(event -> handleAddNewWordItem());
        var addMeaningItem = new JMenuItem("Add meaning");
        addMeaningItem.addActionListener(event -> handleAddMeaningItem(selectedWordId));
        var editWordItem = new JMenuItem("Edit word");
        editWordItem.addActionListener(event -> handleEditWordItem(selectedWordId));
        var deleteWordItem = new JMenuItem("Delete word");
        deleteWordItem.addActionListener(event -> handleDeleteWordItem());

        popupMenu.add(addMeaningItem);
        popupMenu.add(addNewWordItem);
        popupMenu.add(editWordItem);
        popupMenu.add(deleteWordItem);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void handleAddNewWordItem() {
        formRow = -1;
        var panel = new JPanel(new MigLayout());

        var textField = addTextField(panel, "Word", 20);
        var warningLabel = new JLabel();
        warningLabel.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
        warningLabel.setVisible(false);
        panel.add(warningLabel, "cell 0 0");
        textField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                warningLabel.setVisible(wordService.existsByText(textField.getText()));
            }
        });

        var transcriptField = addTextField(panel, "Transcript", 20);
        var frequencyField = addTextField(panel, "Frequency", 20);

        var option = JOptionPane.showOptionDialog(null, panel, "Add new word",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Add", "Cancel"}, null);

        if (option == JOptionPane.OK_OPTION) {
            var now = LocalDateTime.now();
            var frequencyText = frequencyField.getText().strip();
            var transcriptText = transcriptField.getText().strip().replace("/", "");
            var word = new Word();
            word.setText(textField.getText().strip());
            word.setTranscript(transcriptText);
            word.setFrequency(frequencyText.isBlank() ? 0 : Integer.parseInt(frequencyText));
            word.setDateCreated(now);
            word.setLastModified(now);
            var saved = wordService.save(word);
            saved.setMeanings(new ArrayList<>());
            wordsTable.reloadTable(saved);
        }
    }

    private void handleDeleteWordItem() {
        var selectedRow = wordsTable.getSelectedRow();
        var wordDto = wordsTable.getWordTableModel().getWordDtoAt(selectedRow);
        var option = JOptionPane.showOptionDialog(null,
                new JLabel("<html>Are you sure that you want to delete <b>" + wordDto.getText() + "</b> word?"),
                "Confirm word deleting: " + wordDto.getText(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                null, new String[]{"Yes", "Cancel"}, null);

        if (option == JOptionPane.OK_OPTION) {
            wordService.delete(wordDto.getId());
            wordsTable.getWordTableModel().removeRow(selectedRow);
        }
    }

    private void handleAddMeaningItem(long selectedWordId) {
        formRow = -1;
        var panel = new JPanel(new MigLayout());

        var sourceStrings = Arrays.stream(MeaningSource.values())
                .map(MeaningSource::getLabel).toArray(String[]::new);
        var posStrings = Arrays.stream(PartOfSpeech.values())
                .map(PartOfSpeech::getLabel).toArray(String[]::new);
        var statusStrings = Arrays.stream(LearningStatus.values())
                .map(LearningStatus::getLabel).toArray(String[]::new);

        var sourceField = addComboBox(panel, sourceStrings, "Source");
        sourceField.setSelectedItem(MeaningSource.CAMBRIDGE_DICTIONARY.getLabel());
        var targetField = addTextField(panel, "Target");
        var posField = addComboBox(panel, posStrings, "Part of speech");
        var levelField = addComboBox(panel, ProficiencyLevel.values(), "Proficiency level");
        levelField.setSelectedItem(ProficiencyLevel.J7);
        var statusField = addComboBox(panel, statusStrings, "Learning status");
        statusField.setSelectedItem(LearningStatus.LEARNING.getLabel());
        var descriptionField = addTextField(panel, "Description", 50);
        var examplesField = addTextArea(panel, "Examples");

        var word = wordService.getByIdWithMeanings(selectedWordId);
        if (word.isEmpty()) return;

        var option = JOptionPane.showOptionDialog(null, panel,
                "Add new meaning for: " + word.get().getText(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Add", "Cancel"}, null);

        if (option == JOptionPane.OK_OPTION) {
            var now = LocalDateTime.now();
            var meaning = new Meaning();
            meaning.setWordId(selectedWordId);
            meaning.setMeaningSource(MeaningSource.findByLabel((String) sourceField.getSelectedItem()));
            meaning.setTarget(targetField.getText());
            meaning.setPartOfSpeech(PartOfSpeech.findByLabel((String) posField.getSelectedItem()));
            meaning.setProficiencyLevel((ProficiencyLevel) levelField.getSelectedItem());
            meaning.setLearningStatus(LearningStatus.findByLabel((String) statusField.getSelectedItem()));
            meaning.setDescription(descriptionField.getText());
            meaning.setDateCreated(now);
            meaning.setLastModified(now);
            var saved = meaningService.save(meaning);

            Arrays.stream(examplesField.getText().split("\\n"))
                    .filter(text -> !text.isBlank())
                    .forEach(text -> {
                        var ex = new Example();
                        ex.setMeaningId(saved.getId());
                        ex.setText(text);
                        exampleRepository.save(ex);
                    });

            var refreshedWord = wordService.getByIdWithMeanings(selectedWordId);
            refreshedWord.ifPresent(w -> {
                meaningTree.setWord(w);
                meaningTree.showSelectedUserObjectTree();
            });
        }
    }

    private void handleEditWordItem(long selectedWordId) {
        var existingWord = wordService.getById(selectedWordId);
        if (existingWord.isEmpty()) return;
        var word = existingWord.get();

        formRow = -1;
        var panel = new JPanel(new MigLayout());

        var textField = addTextField(panel, "Word", 20);
        textField.setText(word.getText());
        var transcriptField = addTextField(panel, "Transcript", 20);
        transcriptField.setText(word.getTranscript());
        var frequencyField = addTextField(panel, "Frequency", 20);
        frequencyField.setText(word.getFrequency() == 0 ? "" : String.valueOf(word.getFrequency()));

        var option = JOptionPane.showOptionDialog(null, panel,
                "Edit word: " + word.getText(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, new String[]{"Edit", "Cancel"}, null);

        if (option == JOptionPane.OK_OPTION) {
            boolean anyUpdated = false;
            var newText = textField.getText().strip();
            if (!word.getText().equals(newText)) {
                word.setText(newText);
                anyUpdated = true;
            }
            var newTranscript = transcriptField.getText().strip();
            if (!java.util.Objects.equals(word.getTranscript(), newTranscript)) {
                word.setTranscript(newTranscript);
                anyUpdated = true;
            }
            var frequencyText = frequencyField.getText().strip();
            var newFrequency = frequencyText.isBlank() ? 0 : Integer.parseInt(frequencyText);
            if (!java.util.Objects.equals(word.getFrequency(), newFrequency)) {
                word.setFrequency(newFrequency);
                anyUpdated = true;
            }
            if (anyUpdated) {
                word.setLastModified(LocalDateTime.now());
                wordService.save(word);
                wordsTable.reloadTable();
            }
        }
    }

    private <T> JComboBox<T> addComboBox(JPanel panel, T[] values, String labelText) {
        var comboBox = new JComboBox<>(values);
        panel.add(new JLabel(labelText), String.format("cell 0 %s", formRow += 1));
        panel.add(comboBox, String.format("cell 1 %s", formRow));
        return comboBox;
    }

    private JTextField addTextField(JPanel panel, String labelText, int columns) {
        var field = new JTextField(columns);
        panel.add(new JLabel(labelText), String.format("cell 0 %s", formRow += 1));
        panel.add(field, String.format("cell 1 %s", formRow));
        return field;
    }

    private JTextField addTextField(JPanel panel, String labelText) {
        return addTextField(panel, labelText, 40);
    }

    private JTextArea addTextArea(JPanel panel, String labelText) {
        var area = new JTextArea(8, 50);
        panel.add(new JLabel(labelText), String.format("cell 0 %s", formRow += 1));
        panel.add(new JScrollPane(area), String.format("cell 1 %s", formRow));
        return area;
    }
}
