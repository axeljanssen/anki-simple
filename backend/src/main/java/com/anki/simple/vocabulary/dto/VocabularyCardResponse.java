package com.anki.simple.vocabulary.dto;

import com.anki.simple.tag.dto.TagResponse;
import com.anki.simple.vocabulary.LanguageSelection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class  VocabularyCardResponse {
    private Long id;
    private String front;
    private String back;
    private String exampleSentence;
    private LanguageSelection languageSelection;
    private String audioUrl;
    private LocalDateTime createdAt;
    private LocalDateTime lastReviewed;
    private LocalDateTime nextReview;
    private Double easeFactor;
    private Integer intervalDays;
    private Integer repetitions;
    private Set<TagResponse> tags;
}
