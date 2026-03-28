package com.english.eva.repository;

import java.time.LocalDateTime;
import java.util.Set;

import com.english.eva.TestDatabaseConfig;
import com.english.eva.domain.*;
import com.english.eva.model.SearchParams;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordRepositoryTest {

    private DSLContext dsl;
    private WordRepository wordRepository;
    private MeaningRepository meaningRepository;
    private ExampleRepository exampleRepository;

    @BeforeEach
    void setUp() {
        var dbConfig = TestDatabaseConfig.create();
        dsl = dbConfig.getDsl();
        exampleRepository = new ExampleRepository(dsl);
        meaningRepository = new MeaningRepository(dsl, exampleRepository);
        wordRepository = new WordRepository(dsl, meaningRepository);
    }

    @Test
    void save_insertsNewWord() {
        var word = new Word();
        word.setText("hello");
        word.setTranscript("hɛˈloʊ");
        word.setFrequency(500);
        word.setDateCreated(LocalDateTime.now());
        word.setLastModified(LocalDateTime.now());

        var saved = wordRepository.save(word);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("hello");
    }

    @Test
    void save_updatesExistingWord() {
        var word = createAndSaveWord("hello", 500);
        word.setText("world");
        word.setLastModified(LocalDateTime.now());

        wordRepository.save(word);

        var updated = wordRepository.findById(word.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getText()).isEqualTo("world");
    }

    @Test
    void findById_returnsWord() {
        var word = createAndSaveWord("hello", 500);

        var found = wordRepository.findById(word.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("hello");
        assertThat(found.get().getMeanings()).isEmpty();
    }

    @Test
    void findByIdWithMeanings_returnsWordWithMeanings() {
        var word = createAndSaveWord("hello", 500);
        var meaning = new Meaning();
        meaning.setWordId(word.getId());
        meaning.setTarget("greeting");
        meaning.setPartOfSpeech(PartOfSpeech.NOUN);
        meaning.setProficiencyLevel(ProficiencyLevel.A1);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(LearningStatus.KNOWN);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());
        meaningRepository.save(meaning);

        var found = wordRepository.findByIdWithMeanings(word.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMeanings()).hasSize(1);
        assertThat(found.get().getMeanings().get(0).getTarget()).isEqualTo("greeting");
    }

    @Test
    void findAll_returnsSortedByLastModifiedDesc() {
        var earlier = LocalDateTime.now().minusDays(1);
        var later = LocalDateTime.now();

        var word1 = new Word();
        word1.setText("apple");
        word1.setFrequency(0);
        word1.setDateCreated(earlier);
        word1.setLastModified(earlier);
        wordRepository.save(word1);

        var word2 = new Word();
        word2.setText("banana");
        word2.setFrequency(0);
        word2.setDateCreated(later);
        word2.setLastModified(later);
        wordRepository.save(word2);

        var words = wordRepository.findAll();

        assertThat(words).hasSize(2);
        assertThat(words.get(0).getText()).isEqualTo("banana");
        assertThat(words.get(1).getText()).isEqualTo("apple");
    }

    @Test
    void findByIds_returnsMatchingWords() {
        var w1 = createAndSaveWord("hello", 100);
        var w2 = createAndSaveWord("world", 200);
        createAndSaveWord("other", 50);

        var found = wordRepository.findByIds(Set.of(w1.getId(), w2.getId()));

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Word::getText).containsExactlyInAnyOrder("hello", "world");
    }

    @Test
    void delete_removesWord() {
        var word = createAndSaveWord("hello", 500);

        wordRepository.delete(word.getId());

        assertThat(wordRepository.findById(word.getId())).isEmpty();
    }

    @Test
    void existsByText_returnsTrueIfExists() {
        createAndSaveWord("hello", 500);

        assertThat(wordRepository.existsByText("hello")).isTrue();
        assertThat(wordRepository.existsByText("world")).isFalse();
    }

    @Test
    void search_byTextOnly() {
        createAndSaveWord("hello", 100);
        createAndSaveWord("world", 200);

        var results = wordRepository.search(new SearchParams("hel", Set.of(), Set.of()));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("hello");
    }

    @Test
    void search_byLevelAndStatus() {
        var word = createAndSaveWord("hello", 100);
        var meaning = new Meaning();
        meaning.setWordId(word.getId());
        meaning.setTarget("greeting");
        meaning.setPartOfSpeech(PartOfSpeech.NOUN);
        meaning.setProficiencyLevel(ProficiencyLevel.B1);
        meaning.setMeaningSource(MeaningSource.CAMBRIDGE_DICTIONARY);
        meaning.setLearningStatus(LearningStatus.LEARNING);
        meaning.setDateCreated(LocalDateTime.now());
        meaning.setLastModified(LocalDateTime.now());
        meaningRepository.save(meaning);

        var results = wordRepository.search(new SearchParams(
                null, Set.of(ProficiencyLevel.B1), Set.of(LearningStatus.LEARNING)));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getText()).isEqualTo("hello");

        var noResults = wordRepository.search(new SearchParams(
                null, Set.of(ProficiencyLevel.C2), Set.of()));

        assertThat(noResults).isEmpty();
    }

    private Word createAndSaveWord(String text, int frequency) {
        var word = new Word();
        word.setText(text);
        word.setTranscript("");
        word.setFrequency(frequency);
        word.setDateCreated(LocalDateTime.now());
        word.setLastModified(LocalDateTime.now());
        return wordRepository.save(word);
    }
}
