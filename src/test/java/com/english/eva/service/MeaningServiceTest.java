package com.english.eva.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.english.eva.domain.LearningStatus;
import com.english.eva.domain.Meaning;
import com.english.eva.domain.PartOfSpeech;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeaningServiceTest {

    @Mock private DSLContext dsl;
    @Mock private MeaningRepository meaningRepo;
    @Mock private WordRepository wordRepo;

    @InjectMocks private MeaningService meaningService;

    @Test
    void save_delegatesToRepository() {
        var meaning = new Meaning();
        meaning.setTarget("a trial");
        when(meaningRepo.save(any())).thenReturn(meaning);

        meaningService.save(meaning);

        verify(meaningRepo).save(meaning);
    }

    @Test
    void getById_delegatesToRepository() {
        var meaning = new Meaning();
        meaning.setId(1L);
        meaning.setTarget("a trial");
        when(meaningRepo.findById(1L)).thenReturn(Optional.of(meaning));

        var result = meaningService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTarget()).isEqualTo("a trial");
    }

    @Test
    void updateLearningStatus_updatesStatusAndWordTimestamp() {
        var meaning = new Meaning();
        meaning.setId(1L);
        meaning.setWordId(10L);
        when(meaningRepo.findById(1L)).thenReturn(Optional.of(meaning));

        meaningService.updateLearningStatus(1L, LearningStatus.KNOWN);

        verify(meaningRepo).updateLearningStatus(1L, LearningStatus.KNOWN);
        verify(wordRepo).updateLastModified(eq(10L), any(LocalDateTime.class));
    }

    @Test
    void updatePartOfSpeech_updatesPos() {
        meaningService.updatePartOfSpeech(1L, PartOfSpeech.VERB);

        verify(meaningRepo).updatePartOfSpeech(1L, PartOfSpeech.VERB);
    }
}
