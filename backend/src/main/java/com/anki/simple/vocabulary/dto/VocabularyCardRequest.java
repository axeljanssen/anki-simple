package com.anki.simple.vocabulary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class VocabularyCardRequest {
    @NotBlank
    private String front;

    @NotBlank
    private String back;

    private String exampleSentence;
    private String sourceLanguage;
    private String targetLanguage;
    private String audioUrl;
    private Set<Long> tagIds;
}
