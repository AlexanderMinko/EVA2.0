package com.english.eva.ui.word;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

public class WordsTableHeaderRenderer extends JPanel implements TableCellRenderer {

    private final TableCellRenderer defaultRenderer;

    public WordsTableHeaderRenderer(TableCellRenderer defaultRenderer) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        var component = defaultRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        if (component instanceof JLabel label
                && ("Word".equals(label.getText()) || "Frequency".equals(label.getText()))) {
            label.setIcon(null);
            var wordsTable = (WordsTable) table;
            var sortingDetails = wordsTable.getSortingDetails();
            if (label.getText().equals(sortingDetails.getColumnName())) {
                if ("asc".equals(sortingDetails.getDirection())) {
                    label.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
                } else if ("desc".equals(sortingDetails.getDirection())) {
                    label.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
                }
            }
        }
        return component;
    }
}
