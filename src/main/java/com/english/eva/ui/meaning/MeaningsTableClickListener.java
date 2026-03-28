package com.english.eva.ui.meaning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.service.MeaningService;
import com.english.eva.ui.util.UiUtils;

public class MeaningsTableClickListener extends MouseAdapter {

    private final MeaningsTable meaningsTable;
    private final MeaningTree meaningTree;
    private final MeaningService meaningService;
    private final Runnable onRefresh;

    public MeaningsTableClickListener(MeaningsTable meaningsTable, MeaningTree meaningTree,
                                       MeaningService meaningService, Runnable onRefresh) {
        this.meaningsTable = meaningsTable;
        this.meaningTree = meaningTree;
        this.meaningService = meaningService;
        this.onRefresh = onRefresh;
    }

    @Override
    public void mousePressed(MouseEvent event) {
        int row = meaningsTable.rowAtPoint(event.getPoint());
        if (row < 0) return;

        meaningsTable.setRowSelectionInterval(row, row);
        var dto = meaningsTable.getMeaningsTableModel().getMeaningDtoAt(row);

        if (SwingUtilities.isLeftMouseButton(event)) {
            meaningTree.showLoading();
            UiUtils.runInBackground(
                    () -> meaningService.getById(dto.getId()),
                    result -> result.ifPresent(meaningTree::showMeaning)
            );
        } else if (SwingUtilities.isRightMouseButton(event)) {
            showPopupMenu(event, dto.getId());
        }
    }

    private void showPopupMenu(MouseEvent event, long meaningId) {
        var popupMenu = new JPopupMenu();

        Arrays.stream(LearningStatus.values())
                .map(status -> {
                    var item = new JMenuItem(status.getLabel());
                    item.addActionListener(e -> {
                        meaningService.updateLearningStatus(meaningId, status);
                        refreshAfterUpdate(meaningId);
                    });
                    return item;
                })
                .forEach(popupMenu::add);

        var posMenu = new JMenu("Part of Speech");
        Arrays.stream(PartOfSpeech.values())
                .map(pos -> {
                    var item = new JMenuItem(pos.getLabel());
                    item.addActionListener(e -> {
                        meaningService.updatePartOfSpeech(meaningId, pos);
                        refreshAfterUpdate(meaningId);
                    });
                    return item;
                })
                .forEach(posMenu::add);

        popupMenu.addSeparator();
        popupMenu.add(posMenu);
        popupMenu.show(event.getComponent(), event.getX(), event.getY());
    }

    private void refreshAfterUpdate(long meaningId) {
        onRefresh.run();
        meaningService.getById(meaningId).ifPresent(meaningTree::showMeaning);
    }
}
