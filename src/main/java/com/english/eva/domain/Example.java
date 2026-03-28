package com.english.eva.domain;

public class Example {

    private Long id;
    private Long meaningId;
    private String text;

    public Example() {}

    public Example(Long id, Long meaningId, String text) {
        this.id = id;
        this.meaningId = meaningId;
        this.text = text;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMeaningId() { return meaningId; }
    public void setMeaningId(Long meaningId) { this.meaningId = meaningId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
