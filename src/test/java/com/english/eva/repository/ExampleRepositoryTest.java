package com.english.eva.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.english.eva.TestDatabaseConfig;
import com.english.eva.domain.Example;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class ExampleRepositoryTest {

    private DSLContext dsl;
    private ExampleRepository exampleRepository;

    @BeforeEach
    void setUp() {
        var dbConfig = TestDatabaseConfig.create();
        dsl = dbConfig.getDsl();
        exampleRepository = new ExampleRepository(dsl);
        insertTestWord();
        insertTestMeaning();
    }

    private void insertTestWord() {
        dsl.insertInto(table("words"))
                .columns(field("id"), field("text"), field("transcript"),
                        field("frequency"), field("date_created"), field("last_modified"))
                .values(1L, "test", "tɛst", 100, LocalDateTime.now(), LocalDateTime.now())
                .execute();
    }

    private void insertTestMeaning() {
        dsl.insertInto(table("meaning"))
                .columns(field("id"), field("word_id"), field("target"), field("part_of_speech"),
                        field("proficiency_level"), field("meaning_source"), field("learning_status"),
                        field("date_created"), field("last_modified"))
                .values(1L, 1L, "a trial", "NOUN", "B1", "CAMBRIDGE_DICTIONARY", "LEARNING",
                        LocalDateTime.now(), LocalDateTime.now())
                .execute();
    }

    @Test
    void save_insertsNewExample() {
        var example = new Example(null, 1L, "This is a test example.");
        var saved = exampleRepository.save(example);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("This is a test example.");
        assertThat(saved.getMeaningId()).isEqualTo(1L);
    }

    @Test
    void findByMeaningId_returnsExamplesForMeaning() {
        exampleRepository.save(new Example(null, 1L, "Example one"));
        exampleRepository.save(new Example(null, 1L, "Example two"));

        List<Example> examples = exampleRepository.findByMeaningId(1L);

        assertThat(examples).hasSize(2);
        assertThat(examples).extracting(Example::getText)
                .containsExactlyInAnyOrder("Example one", "Example two");
    }

    @Test
    void deleteByMeaningId_removesAllExamplesForMeaning() {
        exampleRepository.save(new Example(null, 1L, "Example one"));
        exampleRepository.save(new Example(null, 1L, "Example two"));

        exampleRepository.deleteByMeaningId(1L);

        assertThat(exampleRepository.findByMeaningId(1L)).isEmpty();
    }

    @Test
    void deleteByMeaningIds_removesExamplesForMultipleMeanings() {
        exampleRepository.save(new Example(null, 1L, "Example one"));

        exampleRepository.deleteByMeaningIds(List.of(1L));

        assertThat(exampleRepository.findByMeaningId(1L)).isEmpty();
    }
}
