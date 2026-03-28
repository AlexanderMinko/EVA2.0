package com.english.eva;

import javax.swing.SwingUtilities;

import com.english.eva.config.DatabaseConfig;
import com.english.eva.repository.ExampleRepository;
import com.english.eva.repository.MeaningRepository;
import com.english.eva.repository.WordRepository;
import com.english.eva.service.MeaningService;
import com.english.eva.service.WordService;
import com.english.eva.ui.frame.ApplicationFrame;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        FlatLightLaf.setup();

        var dbPath = System.getProperty("user.home") + "/English/eva_backup/eva";
        log.info("Starting EVA 2.0 with database: {}", dbPath);

        var dbConfig = new DatabaseConfig(dbPath);
        var dsl = dbConfig.getDsl();

        var exampleRepo = new ExampleRepository(dsl);
        var meaningRepo = new MeaningRepository(dsl, exampleRepo);
        var wordRepo = new WordRepository(dsl, meaningRepo);

        var wordService = new WordService(dsl, wordRepo, meaningRepo, exampleRepo);
        var meaningService = new MeaningService(dsl, meaningRepo, wordRepo);

        SwingUtilities.invokeLater(() -> {
            var frame = new ApplicationFrame(wordService, meaningService, exampleRepo);
            frame.setVisible(true);
            log.info("EVA 2.0 started");
        });
    }
}
