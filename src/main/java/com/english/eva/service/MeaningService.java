package com.english.eva.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.model.MeaningSearchParams;
import com.english.eva.repository.MeaningRepository;
import com.english.eva.repository.WordRepository;
import lombok.Setter;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeaningService {

    private static final Logger log = LoggerFactory.getLogger(MeaningService.class);

    private final DSLContext dsl;
    private final MeaningRepository meaningRepo;
    private final WordRepository wordRepo;
    @Setter
    private WordService wordService;

    public MeaningService(DSLContext dsl, MeaningRepository meaningRepo, WordRepository wordRepo) {
        this.dsl = dsl;
        this.meaningRepo = meaningRepo;
        this.wordRepo = wordRepo;
    }

    public Meaning save(Meaning meaning) {
        log.debug("Saving meaning: {}", meaning.getTarget());
        var saved = meaningRepo.save(meaning);
        if (Objects.nonNull(wordService)) {
            wordService.evictCache(meaning.getWordId());
        }
        return saved;
    }

    public List<Meaning> search(MeaningSearchParams params) {
        return meaningRepo.search(params);
    }

    public long getAllCount() {
        return meaningRepo.count();
    }

    public Optional<Meaning> getById(Long id) {
        return meaningRepo.findById(id);
    }

    public void updateLearningStatus(Long meaningId, LearningStatus status) {
        meaningRepo.updateLearningStatus(meaningId, status);
        meaningRepo.findById(meaningId).ifPresent(meaning -> {
            wordRepo.updateLastModified(meaning.getWordId(), LocalDateTime.now());
            if (Objects.nonNull(wordService)) wordService.evictCache(meaning.getWordId());
        });
        log.debug("Updated learning status for meaning id={} to {}", meaningId, status);
    }

    public void updatePartOfSpeech(Long meaningId, PartOfSpeech pos) {
        meaningRepo.updatePartOfSpeech(meaningId, pos);
        meaningRepo.findById(meaningId).ifPresent(meaning -> {
            if (Objects.nonNull(wordService)) wordService.evictCache(meaning.getWordId());
        });
        log.debug("Updated part of speech for meaning id={} to {}", meaningId, pos);
    }
}
