package com.anki.simple.vocabulary.dto;

import com.anki.simple.vocabulary.LanguageSelection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VocabularyCardLeanResponse {
  private Long id;
  private String front;
  private String back;
  private LanguageSelection languageSelection;
}
