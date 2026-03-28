package com.english.eva.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.PartOfSpeech;
import com.english.eva.repository.MeaningRepository;
import com.english.eva.repository.WordRepository;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeaningService {

    private static final Logger log = LoggerFactory.getLogger(MeaningService.class);

    private final DSLContext dsl;
    private final MeaningRepository meaningRepo;
    private final WordRepository wordRepo;

    public MeaningService(DSLContext dsl, MeaningRepository meaningRepo, WordRepository wordRepo) {
        this.dsl = dsl;
        this.meaningRepo = meaningRepo;
        this.wordRepo = wordRepo;
    }

    public Meaning save(Meaning meaning) {
        log.debug("Saving meaning: {}", meaning.getTarget());
        return meaningRepo.save(meaning);
    }

    public void saveBatch(List<Meaning> meanings) {
        meaningRepo.saveBatch(meanings);
    }

    public Optional<Meaning> getById(Long id) {
        return meaningRepo.findById(id);
    }

    public void updateLearningStatus(Long meaningId, LearningStatus status) {
        meaningRepo.updateLearningStatus(meaningId, status);
        meaningRepo.findById(meaningId).ifPresent(meaning ->
                wordRepo.updateLastModified(meaning.getWordId(), LocalDateTime.now()));
        log.debug("Updated learning status for meaning id={} to {}", meaningId, status);
    }

    public void updatePartOfSpeech(Long meaningId, PartOfSpeech pos) {
        meaningRepo.updatePartOfSpeech(meaningId, pos);
        log.debug("Updated part of speech for meaning id={} to {}", meaningId, pos);
    }
}
