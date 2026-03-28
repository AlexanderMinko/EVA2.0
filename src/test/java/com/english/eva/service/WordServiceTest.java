package com.english.eva.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.english.eva.domain.Word;
import com.english.eva.model.SearchParams;
import com.english.eva.repository.ExampleRepository;
import com.english.eva.repository.MeaningRepository;
import com.english.eva.repository.WordRepository;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock private DSLContext dsl;
    @Mock private WordRepository wordRepo;
    @Mock private MeaningRepository meaningRepo;
    @Mock private ExampleRepository exampleRepo;

    @InjectMocks private WordService wordService;

    @Test
    void getAll_delegatesToRepository() {
        var word = new Word(1L, "test", "tɛst", 100, LocalDateTime.now(), LocalDateTime.now());
        when(wordRepo.findAll()).thenReturn(List.of(word));

        var result = wordService.getAll();

        assertThat(result).hasSize(1);
        verify(wordRepo).findAll();
    }

    @Test
    void getById_delegatesToRepository() {
        var word = new Word(1L, "test", "tɛst", 100, LocalDateTime.now(), LocalDateTime.now());
        when(wordRepo.findById(1L)).thenReturn(Optional.of(word));

        var result = wordService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getText()).isEqualTo("test");
    }

    @Test
    void getByIdWithMeanings_delegatesToRepository() {
        var word = new Word(1L, "test", "tɛst", 100, LocalDateTime.now(), LocalDateTime.now());
        word.setMeanings(List.of());
        when(wordRepo.findByIdWithMeanings(1L)).thenReturn(Optional.of(word));

        var result = wordService.getByIdWithMeanings(1L);

        assertThat(result).isPresent();
        verify(wordRepo).findByIdWithMeanings(1L);
    }

    @Test
    void save_delegatesToRepository() {
        var word = new Word();
        word.setText("test");
        when(wordRepo.save(any())).thenReturn(word);

        wordService.save(word);

        verify(wordRepo).save(word);
    }

    @Test
    void existsByText_delegatesToRepository() {
        when(wordRepo.existsByText("test")).thenReturn(true);

        assertThat(wordService.existsByText("test")).isTrue();
    }

    @Test
    void search_delegatesToRepository() {
        var params = new SearchParams("test", Set.of(), Set.of());
        when(wordRepo.search(params)).thenReturn(List.of());

        wordService.search(params);

        verify(wordRepo).search(params);
    }
}
