package com.english.eva.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.english.eva.domain.*;
import com.english.eva.model.MeaningSearchParams;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class MeaningRepository {

    private final DSLContext dsl;
    private final ExampleRepository exampleRepository;

    public MeaningRepository(DSLContext dsl, ExampleRepository exampleRepository) {
        this.dsl = dsl;
        this.exampleRepository = exampleRepository;
    }

    public Meaning save(Meaning meaning) {
        if (Objects.isNull(meaning.getId())) {
            var record = dsl.insertInto(table("meaning"))
                    .columns(field("word_id"), field("target"), field("transcript"),
                            field("topic"), field("description"), field("part_of_speech"),
                            field("proficiency_level"), field("meaning_source"),
                            field("learning_status"), field("date_created"), field("last_modified"))
                    .values(meaning.getWordId(), meaning.getTarget(), meaning.getTranscript(),
                            meaning.getTopic(), meaning.getDescription(),
                            Objects.nonNull(meaning.getPartOfSpeech()) ? meaning.getPartOfSpeech().name() : null,
                            Objects.nonNull(meaning.getProficiencyLevel()) ? meaning.getProficiencyLevel().name() : null,
                            Objects.nonNull(meaning.getMeaningSource()) ? meaning.getMeaningSource().name() : null,
                            Objects.nonNull(meaning.getLearningStatus()) ? meaning.getLearningStatus().name() : null,
                            meaning.getDateCreated(), meaning.getLastModified())
                    .returningResult(field("id"))
                    .fetchOne();
            meaning.setId(record.get(0, Long.class));
        } else {
            dsl.update(table("meaning"))
                    .set(field("target"), meaning.getTarget())
                    .set(field("transcript"), meaning.getTranscript())
                    .set(field("topic"), meaning.getTopic())
                    .set(field("description"), meaning.getDescription())
                    .set(field("part_of_speech"), Objects.nonNull(meaning.getPartOfSpeech()) ? meaning.getPartOfSpeech().name() : null)
                    .set(field("proficiency_level"), Objects.nonNull(meaning.getProficiencyLevel()) ? meaning.getProficiencyLevel().name() : null)
                    .set(field("meaning_source"), Objects.nonNull(meaning.getMeaningSource()) ? meaning.getMeaningSource().name() : null)
                    .set(field("learning_status"), Objects.nonNull(meaning.getLearningStatus()) ? meaning.getLearningStatus().name() : null)
                    .set(field("last_modified"), meaning.getLastModified())
                    .where(field("id").eq(meaning.getId()))
                    .execute();
        }
        return meaning;
    }

    public void saveBatch(List<Meaning> meanings) {
        for (var meaning : meanings) {
            save(meaning);
        }
    }

    public Optional<Meaning> findById(Long id) {
        return dsl.selectFrom(table("meaning"))
                .where(field("id").eq(id))
                .fetchOptional(this::mapToMeaning);
    }

    public List<Meaning> findByWordId(Long wordId) {
        var meanings = dsl.selectFrom(table("meaning"))
                .where(field("word_id").eq(wordId))
                .fetch(this::mapToMeaning);
        var meaningIds = meanings.stream().map(Meaning::getId).collect(Collectors.toSet());
        var examplesByMeaningId = exampleRepository.findByMeaningIds(meaningIds);
        for (var meaning : meanings) {
            meaning.setExamples(examplesByMeaningId.getOrDefault(meaning.getId(), List.of()));
        }
        return meanings;
    }

    public Map<Long, List<Meaning>> findByWordIds(Set<Long> wordIds) {
        if (wordIds.isEmpty()) return Map.of();
        var meanings = dsl.selectFrom(table("meaning"))
                .where(field("word_id").in(wordIds))
                .fetch(this::mapToMeaning);
        var meaningIds = meanings.stream().map(Meaning::getId).collect(Collectors.toSet());
        var examplesByMeaningId = exampleRepository.findByMeaningIds(meaningIds);
        for (var meaning : meanings) {
            meaning.setExamples(examplesByMeaningId.getOrDefault(meaning.getId(), List.of()));
        }
        var result = new HashMap<Long, List<Meaning>>();
        for (var meaning : meanings) {
            result.computeIfAbsent(meaning.getWordId(), k -> new ArrayList<>()).add(meaning);
        }
        return result;
    }

    public List<Meaning> search(MeaningSearchParams params) {
        Condition condition = DSL.noCondition();
        if (Objects.nonNull(params.getLearningStatus())) {
            condition = condition.and(field("learning_status").eq(params.getLearningStatus().name()));
        }
        if (Objects.nonNull(params.getPartOfSpeech())) {
            condition = condition.and(field("part_of_speech").eq(params.getPartOfSpeech().name()));
        }
        if (Objects.nonNull(params.getProficiencyLevel())) {
            condition = condition.and(field("proficiency_level").eq(params.getProficiencyLevel().name()));
        }
        var meanings = dsl.selectFrom(table("meaning"))
                .where(condition)
                .fetch(this::mapToMeaning);
        var meaningIds = meanings.stream().map(Meaning::getId).collect(Collectors.toSet());
        var examplesByMeaningId = exampleRepository.findByMeaningIds(meaningIds);
        for (var meaning : meanings) {
            meaning.setExamples(examplesByMeaningId.getOrDefault(meaning.getId(), List.of()));
        }
        return meanings;
    }

    public long count() {
        return dsl.fetchCount(table("meaning"));
    }

    public void updateLearningStatus(Long id, LearningStatus status) {
        dsl.update(table("meaning"))
                .set(field("learning_status"), status.name())
                .set(field("last_modified"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }

    public void updatePartOfSpeech(Long id, PartOfSpeech pos) {
        dsl.update(table("meaning"))
                .set(field("part_of_speech"), pos.name())
                .set(field("last_modified"), LocalDateTime.now())
                .where(field("id").eq(id))
                .execute();
    }

    public void deleteByWordId(Long wordId) {
        var meaningIds = dsl.select(field("id"))
                .from(table("meaning"))
                .where(field("word_id").eq(wordId))
                .fetch(r -> r.get(0, Long.class));
        if (!meaningIds.isEmpty()) {
            exampleRepository.deleteByMeaningIds(meaningIds);
        }
        dsl.deleteFrom(table("meaning"))
                .where(field("word_id").eq(wordId))
                .execute();
    }

    private Meaning mapToMeaning(Record r) {
        var meaning = new Meaning();
        meaning.setId(r.get("ID", Long.class));
        meaning.setWordId(r.get("WORD_ID", Long.class));
        meaning.setTarget(r.get("TARGET", String.class));
        meaning.setTranscript(r.get("TRANSCRIPT", String.class));
        meaning.setTopic(r.get("TOPIC", String.class));
        meaning.setDescription(r.get("DESCRIPTION", String.class));
        var pos = r.get("PART_OF_SPEECH", String.class);
        meaning.setPartOfSpeech(Objects.nonNull(pos) ? PartOfSpeech.valueOf(pos) : null);
        var level = r.get("PROFICIENCY_LEVEL", String.class);
        meaning.setProficiencyLevel(Objects.nonNull(level) ? ProficiencyLevel.valueOf(level) : null);
        var source = r.get("MEANING_SOURCE", String.class);
        meaning.setMeaningSource(Objects.nonNull(source) ? MeaningSource.valueOf(source) : null);
        var status = r.get("LEARNING_STATUS", String.class);
        meaning.setLearningStatus(Objects.nonNull(status) ? LearningStatus.valueOf(status) : null);
        meaning.setDateCreated(r.get("DATE_CREATED", LocalDateTime.class));
        meaning.setLastModified(r.get("LAST_MODIFIED", LocalDateTime.class));
        return meaning;
    }
}
