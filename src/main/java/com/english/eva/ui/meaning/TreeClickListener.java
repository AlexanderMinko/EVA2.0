package com.english.eva.ui.meaning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import com.english.eva.ui.word.WordsTable;

public class TreeClickListener extends MouseAdapter {

    private final MeaningTree meaningTree;
    private final WordsTable wordsTable;
    private final MeaningService meaningService;
    private final WordService wordService;

    public TreeClickListener(MeaningTree meaningTree, WordsTable wordsTable,
                              MeaningService meaningService, WordService wordService) {
        this.meaningTree = meaningTree;
        this.wordsTable = wordsTable;
        this.meaningService = meaningService;
        this.wordService = wordService;
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        if (SwingUtilities.isRightMouseButton(event)) {
            doPopup(event);
        }
    }

    private void doPopup(MouseEvent event) {
        var path = meaningTree.getPathForLocation(event.getX(), event.getY());
        if (path == null) return;

        var node = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (node.getUserObject() instanceof MeaningNode meaningNode
                && meaningNode.getType() == MeaningNode.NodeType.MEANING) {
            var id = meaningNode.getMeaningId();
            var popupMenu = new JPopupMenu();

            Arrays.stream(LearningStatus.values())
                    .map(status -> {
                        var item = new JMenuItem(status.getLabel());
                        item.addActionListener(e -> handleLearningUpdate(id, status));
                        return item;
                    })
                    .forEach(popupMenu::add);

            var posMenu = new JMenu("Part of Speech");
            Arrays.stream(PartOfSpeech.values())
                    .map(pos -> {
                        var item = new JMenuItem(pos.getLabel());
                        item.addActionListener(e -> handlePartOfSpeechUpdate(id, pos));
                        return item;
                    })
                    .forEach(posMenu::add);

            popupMenu.addSeparator();
            popupMenu.add(posMenu);
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    private void handleLearningUpdate(long id, LearningStatus status) {
        meaningService.updateLearningStatus(id, status);
        refreshTreeAndTable();
    }

    private void handlePartOfSpeechUpdate(long id, PartOfSpeech pos) {
        meaningService.updatePartOfSpeech(id, pos);
        refreshTreeAndTable();
    }

    private void refreshTreeAndTable() {
        wordService.getByIdWithMeanings(meaningTree.getWord().getId()).ifPresent(word -> {
            meaningTree.setWord(word);
            meaningTree.showSelectedUserObjectTree();
        });
        wordsTable.reloadTable();
    }
}
