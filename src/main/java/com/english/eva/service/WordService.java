package com.english.eva.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.english.eva.domain.Word;
import com.english.eva.model.SearchParams;
import com.english.eva.repository.ExampleRepository;
import com.english.eva.repository.MeaningRepository;
import com.english.eva.repository.WordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WordService {

    private static final Logger log = LoggerFactory.getLogger(WordService.class);

    private final DSLContext dsl;
    private final WordRepository wordRepo;
    private final MeaningRepository meaningRepo;
    private final ExampleRepository exampleRepo;

    public WordService(DSLContext dsl, WordRepository wordRepo,
                       MeaningRepository meaningRepo, ExampleRepository exampleRepo) {
        this.dsl = dsl;
        this.wordRepo = wordRepo;
        this.meaningRepo = meaningRepo;
        this.exampleRepo = exampleRepo;
    }

    public List<Word> getAll() {
        return wordRepo.findAll();
    }

    public Optional<Word> getById(Long id) {
        return wordRepo.findById(id);
    }

    public Optional<Word> getByIdWithMeanings(Long id) {
        return wordRepo.findByIdWithMeanings(id);
    }

    public Word save(Word word) {
        log.debug("Saving word: {}", word.getText());
        return wordRepo.save(word);
    }

    public void delete(Long id) {
        dsl.transaction(ctx -> {
            meaningRepo.deleteByWordId(id);
            wordRepo.delete(id);
        });
        log.debug("Deleted word id={}", id);
    }

    public List<Word> search(SearchParams params) {
        return wordRepo.search(params);
    }

    public List<Word> getByWordIds(Set<Long> ids) {
        return wordRepo.findByIds(ids);
    }

    public boolean existsByText(String text) {
        return wordRepo.existsByText(text);
    }

    public void exportToJson(Path path) {
        try {
            var words = wordRepo.findAll();
            for (var word : words) {
                word.setMeanings(meaningRepo.findByWordId(word.getId()));
            }
            var mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            Files.createDirectories(path.getParent());
            mapper.writeValue(path.toFile(), words);
            log.info("Exported {} words to {}", words.size(), path);
        } catch (IOException e) {
            log.error("Failed to export words to JSON", e);
        }
    }
}
