package com.english.eva.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.english.eva.TestDatabaseConfig;
import com.english.eva.domain.*;
import com.english.eva.model.MeaningSearchParams;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

class MeaningRepositoryTest {

    private DSLContext dsl;
    private MeaningRepository meaningRepository;
    private ExampleRepository exampleRepository;

    @BeforeEach
    void setUp() {
        var dbConfig = TestDatabaseConfig.create();
        dsl = dbConfig.getDsl();
        exampleRepository = new ExampleRepository(dsl);
        meaningRepository = new MeaningRepository(dsl, exampleRepository);
        insertTestWord();
    }

    private void insertTestWord() {
        dsl.insertInto(table("words"))
                .columns(field("id"), field("text"), field("transcript"),
                        field("frequency"), field("date_created"), field("last_modified"))
                .values(1L, "test", "tɛst", 100, LocalDateTime.now(), LocalDateTime.now())
                .execute();
    }

    @Test
    void save_insertsNewMeaning() {
        var meaning = new Meaning();
        meaning.setWordId(1L);
        meaning.setTarget("a trial");
        meaning.setPartOfSpeech(PartOfSpeech.NOUN);
        meaning.setProficiencyLevel(ProficiencyLevel.B1);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(LearningStatus.LEARNING);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());

        var saved = meaningRepository.save(meaning);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTarget()).isEqualTo("a trial");
    }

    @Test
    void findById_returnsMeaning() {
        var meaning = createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

        var found = meaningRepository.findById(meaning.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTarget()).isEqualTo("a trial");
        assertThat(found.get().getPartOfSpeech()).isEqualTo(PartOfSpeech.NOUN);
    }

    @Test
    void findByWordId_returnsMeaningsWithExamples() {
        var meaning = createAndSaveMeaning("a trial", PartOfSpeech.NOUN);
        exampleRepository.save(new Example(null, meaning.getId(), "This is a test."));

        var meanings = meaningRepository.findByWordId(1L);

        assertThat(meanings).hasSize(1);
        assertThat(meanings.get(0).getExamples()).hasSize(1);
        assertThat(meanings.get(0).getExamples().get(0).getText()).isEqualTo("This is a test.");
    }

    @Test
    void updateLearningStatus_updatesStatus() {
        var meaning = createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

        meaningRepository.updateLearningStatus(meaning.getId(), LearningStatus.KNOWN);

        var updated = meaningRepository.findById(meaning.getId());
        assertThat(updated.get().getLearningStatus()).isEqualTo(LearningStatus.KNOWN);
    }

    @Test
    void updatePartOfSpeech_updatesPos() {
        var meaning = createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

        meaningRepository.updatePartOfSpeech(meaning.getId(), PartOfSpeech.VERB);

        var updated = meaningRepository.findById(meaning.getId());
        assertThat(updated.get().getPartOfSpeech()).isEqualTo(PartOfSpeech.VERB);
    }

    @Test
    void deleteByWordId_removesAllMeaningsAndExamples() {
        var meaning = createAndSaveMeaning("a trial", PartOfSpeech.NOUN);
        exampleRepository.save(new Example(null, meaning.getId(), "Example text"));

        meaningRepository.deleteByWordId(1L);

        assertThat(meaningRepository.findByWordId(1L)).isEmpty();
        assertThat(exampleRepository.findByMeaningId(meaning.getId())).isEmpty();
    }

    @Test
    void saveBatch_insertsMultipleMeanings() {
        var m1 = new Meaning();
        m1.setWordId(1L);
        m1.setTarget("meaning one");
        m1.setPartOfSpeech(PartOfSpeech.NOUN);
        m1.setProficiencyLevel(ProficiencyLevel.A1);
        m1.setMeaningSource(MeaningSource.GOOGLE);
        m1.setLearningStatus(LearningStatus.UNDEFINED);
        m1.setDateCreated(LocalDateTime.now());
        m1.setLastModified(LocalDateTime.now());

        var m2 = new Meaning();
        m2.setWordId(1L);
        m2.setTarget("meaning two");
        m2.setPartOfSpeech(PartOfSpeech.VERB);
        m2.setProficiencyLevel(ProficiencyLevel.B2);
        m2.setMeaningSource(MeaningSource.GOOGLE);
        m2.setLearningStatus(LearningStatus.LEARNING);
        m2.setDateCreated(LocalDateTime.now());
        m2.setLastModified(LocalDateTime.now());

        meaningRepository.saveBatch(List.of(m1, m2));

        assertThat(meaningRepository.findByWordId(1L)).hasSize(2);
    }

    @Test
    void search_byText_filtersByTarget() {
        createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
        createAndSaveMeaning("carry on", PartOfSpeech.PHRASAL_VERB);
        createAndSaveMeaning("give up", PartOfSpeech.PHRASAL_VERB);

        var params = new MeaningSearchParams();
        params.setText("carry");

        var results = meaningRepository.search(params);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Meaning::getTarget)
                .containsExactlyInAnyOrder("carry out", "carry on");
    }

    @Test
    void search_byMultipleLevels_filtersCorrectly() {
        createAndSaveMeaningWithLevel("word1", ProficiencyLevel.A1);
        createAndSaveMeaningWithLevel("word2", ProficiencyLevel.B2);
        createAndSaveMeaningWithLevel("word3", ProficiencyLevel.C1);

        var params = new MeaningSearchParams();
        params.setProficiencyLevels(Set.of(ProficiencyLevel.A1, ProficiencyLevel.B2));

        var results = meaningRepository.search(params);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Meaning::getProficiencyLevel)
                .containsExactlyInAnyOrder(ProficiencyLevel.A1, ProficiencyLevel.B2);
    }

    @Test
    void search_byMultipleStatuses_filtersCorrectly() {
        createAndSaveMeaningWithStatus("known-word", LearningStatus.KNOWN);
        createAndSaveMeaningWithStatus("learning-word", LearningStatus.LEARNING);
        createAndSaveMeaningWithStatus("putoff-word", LearningStatus.PUT_OFF);

        var params = new MeaningSearchParams();
        params.setLearningStatuses(Set.of(LearningStatus.KNOWN, LearningStatus.LEARNING));

        var results = meaningRepository.search(params);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Meaning::getLearningStatus)
                .containsExactlyInAnyOrder(LearningStatus.KNOWN, LearningStatus.LEARNING);
    }

    @Test
    void search_byPartOfSpeech_filtersCorrectly() {
        createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
        createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

        var params = new MeaningSearchParams();
        params.setPartOfSpeech(PartOfSpeech.PHRASAL_VERB);

        var results = meaningRepository.search(params);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTarget()).isEqualTo("carry out");
    }

    @Test
    void search_combinedFilters_narrowsResults() {
        createAndSaveMeaning("carry out", PartOfSpeech.PHRASAL_VERB);
        createAndSaveMeaning("carry on", PartOfSpeech.PHRASAL_VERB);
        createAndSaveMeaning("a trial", PartOfSpeech.NOUN);

        var params = new MeaningSearchParams();
        params.setText("carry");
        params.setPartOfSpeech(PartOfSpeech.PHRASAL_VERB);

        var results = meaningRepository.search(params);

        assertThat(results).hasSize(2);
    }

    private Meaning createAndSaveMeaningWithLevel(String target, ProficiencyLevel level) {
        var meaning = new Meaning();
        meaning.setWordId(1L);
        meaning.setTarget(target);
        meaning.setPartOfSpeech(PartOfSpeech.NOUN);
        meaning.setProficiencyLevel(level);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(LearningStatus.LEARNING);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());
        return meaningRepository.save(meaning);
    }

    private Meaning createAndSaveMeaningWithStatus(String target, LearningStatus status) {
        var meaning = new Meaning();
        meaning.setWordId(1L);
        meaning.setTarget(target);
        meaning.setPartOfSpeech(PartOfSpeech.NOUN);
        meaning.setProficiencyLevel(ProficiencyLevel.B1);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(status);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());
        return meaningRepository.save(meaning);
    }

    private Meaning createAndSaveMeaning(String target, PartOfSpeech pos) {
        var meaning = new Meaning();
        meaning.setWordId(1L);
        meaning.setTarget(target);
        meaning.setPartOfSpeech(pos);
        meaning.setProficiencyLevel(ProficiencyLevel.B1);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(LearningStatus.LEARNING);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());
        return meaningRepository.save(meaning);
    }
}
