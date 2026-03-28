package com.english.eva.ui.word;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.english.eva.domain.Word;

public class WordTableModel extends AbstractTableModel {

    public static final int COLUMN_WORD = 0;
    public static final int COLUMN_TRANSCRIPT = 1;
    public static final int COLUMN_FREQUENCY = 2;
    public static final int COLUMN_PROGRESS = 3;
    public static final int COLUMN_LEVELS = 4;
    public static final int COLUMN_PARTS_OF_SPEECH = 5;

    private static final String[] COLUMN_NAMES = {
            "Word", "Transcript", "Frequency", "Progress", "Levels", "Parts of speech"
    };

    private List<WordDto> wordDtoList = new ArrayList<>();

    public WordTableModel(List<Word> words) {
        setData(words);
    }

    public void setData(List<Word> words) {
        this.wordDtoList = words.stream().map(WordDto::new).toList();
        fireTableDataChanged();
    }

    public WordDto getWordDtoAt(int row) {
        return wordDtoList.get(row);
    }

    public void removeRow(int row) {
        wordDtoList = new ArrayList<>(wordDtoList);
        wordDtoList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    @Override
    public int getRowCount() {
        return wordDtoList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        var dto = wordDtoList.get(rowIndex);
        return switch (columnIndex) {
            case COLUMN_WORD -> dto.getText();
            case COLUMN_TRANSCRIPT -> dto.getTranscript();
            case COLUMN_FREQUENCY -> dto.getFrequency();
            case COLUMN_PROGRESS -> dto.getProgress();
            case COLUMN_LEVELS -> dto.getLevels();
            case COLUMN_PARTS_OF_SPEECH -> dto.getPartsOfSpeech();
            default -> throw new IllegalStateException("Unexpected column: " + columnIndex);
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
