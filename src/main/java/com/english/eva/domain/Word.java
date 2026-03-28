package com.english.eva.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Word implements Comparable<Word> {

    private Long id;
    private String text;
    private String transcript;
    private Integer frequency;
    private LocalDateTime dateCreated;
    private LocalDateTime lastModified;
    private List<Meaning> meanings = new ArrayList<>();

    public Word() {}

    public Word(Long id, String text, String transcript, Integer frequency,
                LocalDateTime dateCreated, LocalDateTime lastModified) {
        this.id = id;
        this.text = text;
        this.transcript = transcript;
        this.frequency = frequency;
        this.dateCreated = dateCreated;
        this.lastModified = lastModified;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public List<Meaning> getMeanings() { return meanings; }
    public void setMeanings(List<Meaning> meanings) { this.meanings = meanings; }

    @Override
    public int compareTo(Word other) {
        return other.lastModified.compareTo(this.lastModified);
    }
}
