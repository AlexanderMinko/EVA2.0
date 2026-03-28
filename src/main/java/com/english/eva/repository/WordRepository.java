package com.english.eva.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.english.eva.domain.Word;
import com.english.eva.model.SearchParams;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class WordRepository {

    private final DSLContext dsl;
    private final MeaningRepository meaningRepository;

    public WordRepository(DSLContext dsl, MeaningRepository meaningRepository) {
        this.dsl = dsl;
        this.meaningRepository = meaningRepository;
    }

    public Word save(Word word) {
        if (word.getId() == null) {
            var record = dsl.insertInto(table("words"))
                    .columns(field("text"), field("transcript"), field("frequency"),
                            field("date_created"), field("last_modified"))
                    .values(word.getText(), word.getTranscript(), word.getFrequency(),
                            word.getDateCreated(), word.getLastModified())
                    .returningResult(field("id"))
                    .fetchOne();
            word.setId(record.get(0, Long.class));
        } else {
            dsl.update(table("words"))
                    .set(field("text"), word.getText())
                    .set(field("transcript"), word.getTranscript())
                    .set(field("frequency"), word.getFrequency())
                    .set(field("last_modified"), word.getLastModified())
                    .where(field("id").eq(word.getId()))
                    .execute();
        }
        return word;
    }

    public Optional<Word> findById(Long id) {
        return dsl.selectFrom(table("words"))
                .where(field("id").eq(id))
                .fetchOptional(this::mapToWord);
    }

    public Optional<Word> findByIdWithMeanings(Long id) {
        return findById(id).map(word -> {
            word.setMeanings(meaningRepository.findByWordId(id));
            return word;
        });
    }

    public List<Word> findAll() {
        var words = dsl.selectFrom(table("words"))
                .orderBy(field("last_modified").desc())
                .fetch(this::mapToWord);
        for (var word : words) {
            word.setMeanings(meaningRepository.findByWordId(word.getId()));
        }
        return words;
    }

    public List<Word> findByIds(Set<Long> ids) {
        if (ids.isEmpty()) return List.of();
        var words = dsl.selectFrom(table("words"))
                .where(field("id").in(ids))
                .fetch(this::mapToWord);
        for (var word : words) {
            word.setMeanings(meaningRepository.findByWordId(word.getId()));
        }
        return words;
    }

    public List<Word> search(SearchParams params) {
        var words = table("words");
        var meaning = table("meaning");
        boolean needsJoin = !params.levels().isEmpty() || !params.statuses().isEmpty();

        Condition condition = DSL.noCondition();

        if (params.text() != null && !params.text().isBlank()) {
            condition = condition.and(field("words.text").likeIgnoreCase("%" + params.text() + "%"));
        }
        if (!params.levels().isEmpty()) {
            var levelNames = params.levels().stream().map(Enum::name).collect(Collectors.toSet());
            condition = condition.and(field("meaning.proficiency_level").in(levelNames));
        }
        if (!params.statuses().isEmpty()) {
            var statusNames = params.statuses().stream().map(Enum::name).collect(Collectors.toSet());
            condition = condition.and(field("meaning.learning_status").in(statusNames));
        }

        List<Word> result;
        if (needsJoin) {
            result = dsl.selectDistinct(
                            field("words.id").as("ID"),
                            field("words.text").as("TEXT"),
                            field("words.transcript").as("TRANSCRIPT"),
                            field("words.frequency").as("FREQUENCY"),
                            field("words.date_created").as("DATE_CREATED"),
                            field("words.last_modified").as("LAST_MODIFIED"))
                    .from(words)
                    .join(meaning).on(field("meaning.word_id").eq(field("words.id")))
                    .where(condition)
                    .orderBy(field("words.last_modified").desc())
                    .fetch(this::mapToWord);
        } else {
            result = dsl.selectFrom(words)
                    .where(condition)
                    .orderBy(field("last_modified").desc())
                    .fetch(this::mapToWord);
        }
        for (var word : result) {
            word.setMeanings(meaningRepository.findByWordId(word.getId()));
        }
        return result;
    }

    public void delete(Long id) {
        dsl.deleteFrom(table("words"))
                .where(field("id").eq(id))
                .execute();
    }

    public boolean existsByText(String text) {
        return dsl.fetchExists(
                dsl.selectFrom(table("words"))
                        .where(field("text").eq(text))
        );
    }

    public void updateLastModified(Long id, LocalDateTime lastModified) {
        dsl.update(table("words"))
                .set(field("last_modified"), lastModified)
                .where(field("id").eq(id))
                .execute();
    }

    private Word mapToWord(Record r) {
        return new Word(
                r.get("ID", Long.class),
                r.get("TEXT", String.class),
                r.get("TRANSCRIPT", String.class),
                r.get("FREQUENCY", Integer.class),
                r.get("DATE_CREATED", LocalDateTime.class),
                r.get("LAST_MODIFIED", LocalDateTime.class)
        );
    }
}
