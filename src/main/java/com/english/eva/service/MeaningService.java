package com.english.eva.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final int CACHE_MAX_SIZE = 200;

    private final Map<Long, Meaning> meaningCache = new LinkedHashMap<>(CACHE_MAX_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Meaning> eldest) {
            return size() > CACHE_MAX_SIZE;
        }
    };

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
        meaningCache.remove(saved.getId());
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
        var cached = meaningCache.get(id);
        if (Objects.nonNull(cached)) {
            log.debug("Cache hit for meaning id={}", id);
            return Optional.of(cached);
        }
        var result = meaningRepo.findById(id);
        result.ifPresent(meaning -> meaningCache.put(id, meaning));
        return result;
    }

    public void evictMeaningCache(Long meaningId) {
        meaningCache.remove(meaningId);
    }

    public void updateLearningStatus(Long meaningId, LearningStatus status) {
        meaningRepo.updateLearningStatus(meaningId, status);
        meaningCache.remove(meaningId);
        meaningRepo.findById(meaningId).ifPresent(meaning -> {
            wordRepo.updateLastModified(meaning.getWordId(), LocalDateTime.now());
            if (Objects.nonNull(wordService)) wordService.evictCache(meaning.getWordId());
        });
        log.debug("Updated learning status for meaning id={} to {}", meaningId, status);
    }

    public void updatePartOfSpeech(Long meaningId, PartOfSpeech pos) {
        meaningRepo.updatePartOfSpeech(meaningId, pos);
        meaningCache.remove(meaningId);
        meaningRepo.findById(meaningId).ifPresent(meaning -> {
            if (Objects.nonNull(wordService)) wordService.evictCache(meaning.getWordId());
        });
        log.debug("Updated part of speech for meaning id={} to {}", meaningId, pos);
    }
}
