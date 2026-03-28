package com.english.eva.ui.word;

import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class WordTableCellRenderer extends JPanel implements TableCellRenderer {

    private final DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer();

    public WordTableCellRenderer() {
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

        if (column == WordTableModel.COLUMN_WORD) {
            var label = new JLabel(" " + stringValue);
            label.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            add(label, BorderLayout.CENTER);
        } else if (column == WordTableModel.COLUMN_PROGRESS) {
            var progressBar = new JProgressBar();
            progressBar.setValue(Integer.parseInt(stringValue));
            progressBar.setStringPainted(true);
            progressBar.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
            progressBar.setBorder(new EmptyBorder(1, 1, 1, 1));
            add(progressBar);
        } else if (column == WordTableModel.COLUMN_LEVELS) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            if (!stringValue.isBlank()) {
                for (String level : stringValue.split(" ")) {
                    var levelLabel = new JLabel(level);
                    levelLabel.setLayout(new BorderLayout());
                    levelLabel.setOpaque(true);
                    levelLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize() - 2));
                    levelLabel.setBackground(LEVEL_COLOURS.get(level));
                    levelLabel.setForeground(Color.WHITE);
                    levelLabel.setBorder(new EmptyBorder(2, 3, 0, 3));
                    add(levelLabel);
                    add(Box.createRigidArea(new Dimension(5, 0)));
                }
            }
        } else {
            var label = new JLabel(stringValue);
            add(label, BorderLayout.CENTER);
        }
        return this;
    }
}
