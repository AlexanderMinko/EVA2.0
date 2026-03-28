package com.english.eva.ui.meaning;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.english.eva.domain.Meaning;

public class MeaningsTableModel extends AbstractTableModel {

    public static final int COLUMN_MEANING = 0;
    public static final int COLUMN_LEVEL = 1;
    public static final int COLUMN_STATUS = 2;
    public static final int COLUMN_PART_OF_SPEECH = 3;

    private static final String[] COLUMN_NAMES = {
            "Meaning", "Level", "Status", "Part of Speech"
    };

    private List<MeaningDto> meaningDtoList = new ArrayList<>();

    public void setData(List<Meaning> meanings) {
        this.meaningDtoList = meanings.stream().map(MeaningDto::new).toList();
        fireTableDataChanged();
    }

    public MeaningDto getMeaningDtoAt(int row) {
        return meaningDtoList.get(row);
    }

    @Override
    public int getRowCount() {
        return meaningDtoList.size();
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
        var dto = meaningDtoList.get(rowIndex);
        return switch (columnIndex) {
            case COLUMN_MEANING -> dto.getTarget();
            case COLUMN_LEVEL -> dto.getLevel();
            case COLUMN_STATUS -> dto.getStatus();
            case COLUMN_PART_OF_SPEECH -> dto.getPartOfSpeech();
            default -> throw new IllegalStateException("Unexpected column: " + columnIndex);
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
