package com.english.eva.ui.meaning;

import static com.english.eva.ui.util.ColorUtils.LEARNING_COLOURS;
import static com.english.eva.ui.util.ColorUtils.LEVEL_COLOURS;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

public class MeaningTreeCellRenderer extends JPanel implements TreeCellRenderer {

    private final DefaultTreeCellRenderer defaultRenderer = new DefaultTreeCellRenderer();

    public MeaningTreeCellRenderer() {
        setLayout(new BorderLayout());
        setOpaque(false);
    }

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        var renderer = (DefaultTreeCellRenderer) defaultRenderer.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        var keyField = new JLabel();
        var valueField = new JLabel();
        var font = renderer.getFont();

        var node = (DefaultMutableTreeNode) value;
        var userObject = node.getUserObject();

        if (userObject instanceof MeaningNode meaningNode) {
            removeAll();
            switch (meaningNode.getType()) {
                case MEANING -> {
                    keyField.setText(meaningNode.getDisplayText() + " ");
                    keyField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                    var statusLabel = Objects.nonNull(meaningNode.getLearningStatus())
                            ? meaningNode.getLearningStatus().getLabel() : "";
                    valueField.setText(statusLabel);
                    valueField.setOpaque(true);
                    valueField.setBackground(LEARNING_COLOURS.get(statusLabel));
                    valueField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                    valueField.setBorder(new EmptyBorder(3, 3, 3, 3));
                }
                case DESCRIPTION -> {
                    var level = Objects.nonNull(meaningNode.getProficiencyLevel())
                            ? meaningNode.getProficiencyLevel().name() : "";
                    keyField.setText(level);
                    keyField.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));
                    keyField.setOpaque(true);
                    keyField.setBorder(new EmptyBorder(0, 10, 0, 10));
                    keyField.setBackground(LEVEL_COLOURS.get(level));
                    keyField.setForeground(Color.WHITE);
                    valueField.setText(" " + meaningNode.getDescription());
                    valueField.setFont(new Font(font.getName(), font.getStyle(), font.getSize() + 2));
                }
                case SOURCE, PART_OF_SPEECH -> {
                    var path = tree.getPathForRow(row);
                    boolean isTargetRow = Objects.nonNull(path) && (path.getPath().length == 3 || path.getPath().length == 4);
                    keyField.setText(meaningNode.getDisplayText());
                    keyField.setFont(isTargetRow
                            ? new Font(font.getName(), Font.BOLD, font.getSize())
                            : font);
                    valueField.setText(null);
                }
                case EXAMPLES_HEADER -> {
                    keyField.setText(meaningNode.getDisplayText());
                    keyField.setFont(new Font(font.getName(), Font.ITALIC, font.getSize() - 1));
                    valueField.setText(null);
                }
                case EXAMPLE -> {
                    keyField.setText(meaningNode.getDisplayText());
                    keyField.setFont(font);
                    valueField.setText(null);
                }
            }
            add(keyField, BorderLayout.WEST);
            add(valueField, BorderLayout.CENTER);
            return this;
        }

        return renderer;
    }
}
