package com.english.eva.ui.panel;

import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.english.eva.repository.ExampleRepository;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import com.english.eva.ui.meaning.MeaningTree;
import com.english.eva.ui.meaning.TreeClickListener;
import com.english.eva.ui.settings.SettingsPanel;
import com.english.eva.ui.word.SortingDetails;
import com.english.eva.ui.word.TableClickListener;
import com.english.eva.ui.word.WordsTable;
import net.miginfocom.swing.MigLayout;

public class VocabularyPanel extends JPanel {

    public VocabularyPanel(WordService wordService, MeaningService meaningService,
                           ExampleRepository exampleRepository) {
        setLayout(new MigLayout("fill"));
        setPreferredSize(new Dimension(600, 600));

        var meaningTree = new MeaningTree();
        var wordsTable = new WordsTable(wordService);

        var tableClickListener = new TableClickListener(wordsTable, meaningTree, wordService, meaningService, exampleRepository);
        wordsTable.setTableClickListener(tableClickListener);

        var treeClickListener = new TreeClickListener(meaningTree, wordsTable, meaningService, wordService);
        meaningTree.addMouseListener(treeClickListener);

        var mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.3);
        mainSplitPane.setLeftComponent(new JScrollPane(wordsTable));
        mainSplitPane.setRightComponent(new JScrollPane(meaningTree));

        var settingsPanel = new SettingsPanel(searchParams -> {
            var results = wordService.search(searchParams);
            wordsTable.reloadTable(results);
            wordsTable.setSortingDetails(new SortingDetails());
        });

        add(settingsPanel, "cell 0 0, pushx");
        add(mainSplitPane, "cell 0 1, push, grow");
    }
}
