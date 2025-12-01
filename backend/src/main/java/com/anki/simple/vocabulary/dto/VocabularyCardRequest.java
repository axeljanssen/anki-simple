package com.anki.simple.vocabulary.dto;

import com.anki.simple.vocabulary.LanguageSelection;
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
    private LanguageSelection languageSelection;
    private String audioUrl;
    private Set<Long> tagIds;
}
