package com.english.eva.ui.word;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WordsHeaderClickListener extends MouseAdapter {

    private final WordsTable wordsTable;

    public WordsHeaderClickListener(WordsTable wordsTable) {
        this.wordsTable = wordsTable;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        var point = event.getPoint();
        var column = wordsTable.columnAtPoint(point);
        switch (column) {
            case WordTableModel.COLUMN_WORD -> setSortingDetails("Word");
            case WordTableModel.COLUMN_FREQUENCY -> setSortingDetails("Frequency");
            default -> {}
        }
    }

    private void setSortingDetails(String columnName) {
        var sortingDetails = wordsTable.getSortingDetails();
        if (!columnName.equals(sortingDetails.getColumnName())) {
            sortingDetails.setDirection("asc");
        } else if ("asc".equals(sortingDetails.getDirection())) {
            sortingDetails.setDirection("desc");
        } else {
            sortingDetails.setDirection("asc");
        }
        sortingDetails.setColumnName(columnName);
        wordsTable.sortData();
    }
}
