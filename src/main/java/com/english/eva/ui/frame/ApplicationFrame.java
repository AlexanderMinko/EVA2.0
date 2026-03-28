package com.english.eva.ui.frame;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.english.eva.repository.ExampleRepository;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import com.english.eva.ui.panel.VocabularyPanel;
import com.english.eva.ui.panel.WordGymPanel;

public class ApplicationFrame extends JFrame {

    public ApplicationFrame(WordService wordService, MeaningService meaningService,
                            ExampleRepository exampleRepository) {
        super("English Vocabulary Assistant 2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        var rootTab = new JTabbedPane();
        rootTab.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        rootTab.addTab("Vocabulary", new VocabularyPanel(wordService, meaningService, exampleRepository));
        rootTab.addTab("Word Gym", new WordGymPanel(meaningService));
        rootTab.setSelectedIndex(0);
        add(rootTab, BorderLayout.CENTER);
    }
}
