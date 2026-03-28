package com.english.eva.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Word implements Comparable<Word> {

    private Long id;
    private String text;
    private String transcript;
    private Integer frequency;
    private LocalDateTime dateCreated;
    private LocalDateTime lastModified;
    private List<Meaning> meanings = new ArrayList<>();

    public Word(Long id, String text, String transcript, Integer frequency,
                LocalDateTime dateCreated, LocalDateTime lastModified) {
        this.id = id;
        this.text = text;
        this.transcript = transcript;
        this.frequency = frequency;
        this.dateCreated = dateCreated;
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(Word other) {
        return other.lastModified.compareTo(this.lastModified);
    }
}
