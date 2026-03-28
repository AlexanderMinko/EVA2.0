package com.english.eva.ui.meaning;

import static com.english.eva.ui.util.ColorUtils.LEARNING_COLOURS;
import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class MeaningsTableCellRenderer extends JPanel implements TableCellRenderer {

    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

    public MeaningsTableCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
        var renderer = defaultRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
        setBackground(isSelected ? Color.LIGHT_GRAY : table.getBackground());
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, table.getGridColor()));
        var font = renderer.getFont();
        var stringValue = String.valueOf(value);
        removeAll();

        if (column == MeaningsTableModel.COLUMN_MEANING) {
            var label = new JLabel(" " + stringValue);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            add(label, BorderLayout.CENTER);
        } else if (column == MeaningsTableModel.COLUMN_LEVEL) {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize() - 2));
            if (!stringValue.isBlank() && LEVEL_COLOURS.containsKey(stringValue)) {
                label.setOpaque(true);
                label.setBackground(LEVEL_COLOURS.get(stringValue));
                label.setForeground(Color.WHITE);
                label.setBorder(new EmptyBorder(2, 3, 0, 3));
            }
            add(label, BorderLayout.CENTER);
        } else if (column == MeaningsTableModel.COLUMN_STATUS) {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            if (!stringValue.isBlank() && LEARNING_COLOURS.containsKey(stringValue)) {
                label.setOpaque(true);
                label.setBackground(LEARNING_COLOURS.get(stringValue));
            }
            label.setBorder(new EmptyBorder(3, 3, 3, 3));
            add(label, BorderLayout.CENTER);
        } else {
            var label = new JLabel(stringValue);
            label.setHorizontalAlignment(JLabel.CENTER);
            add(label, BorderLayout.CENTER);
        }
        return this;
    }
}
