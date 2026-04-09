package com.english.eva.ui.panel;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.english.eva.service.MeaningService;
import com.english.eva.ui.meaning.MeaningTree;
import com.english.eva.ui.meaning.MeaningsTable;
import com.english.eva.ui.meaning.MeaningsTableClickListener;
import com.english.eva.ui.settings.MeaningSettingsPanel;
import com.english.eva.ui.util.UiUtils;
import net.miginfocom.swing.MigLayout;

public class MeaningsPanel extends JPanel {

    public MeaningsPanel(MeaningService meaningService) {
        setLayout(new MigLayout("fill"));
        setPreferredSize(new Dimension(600, 600));

        var meaningTree = new MeaningTree();
        var meaningsTable = new MeaningsTable();

        var settingsPanel = new MeaningSettingsPanel(searchParams ->
                UiUtils.runInBackground(
                        () -> meaningService.search(searchParams),
                        meaningsTable::reloadTable
                )
        );

        var clickListener = new MeaningsTableClickListener(
                meaningsTable, meaningTree, meaningService,
                () -> settingsPanel.triggerSearch()
        );
        meaningsTable.addMouseListener(clickListener);

        var mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setLeftComponent(new JScrollPane(meaningsTable));
        mainSplitPane.setRightComponent(new JScrollPane(meaningTree));

        add(settingsPanel, "cell 0 0, pushx");
        add(mainSplitPane, "cell 0 1, push, grow");

        settingsPanel.triggerSearch();
    }
}
