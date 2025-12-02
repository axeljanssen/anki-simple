package com.anki.simple.vocabulary.mapper;

import com.anki.simple.vocabulary.LanguageSelection;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.dto.VocabularyCardLeanResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("VocabularyCardMapper Tests")
class VocabularyCardMapperTest {

  @Autowired
  private VocabularyCardMapper vocabularyCardMapper;

  @Test
  @DisplayName("Should map VocabularyCard to VocabularyCardLeanResponse")
  void shouldMapToLeanResponse() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setId(1L);
    card.setFront("Hello");
    card.setBack("Hola");
    card.setExampleSentence("Hello, how are you?");
    card.setLanguageSelection(LanguageSelection.DE_ES);
    card.setAudioUrl("http://example.com/audio.mp3");
    card.setEaseFactor(2.5);
    card.setIntervalDays(5);
    card.setRepetitions(3);

    // When
    VocabularyCardLeanResponse response = vocabularyCardMapper.toLeanResponse(card);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getFront()).isEqualTo("Hello");
    assertThat(response.getBack()).isEqualTo("Hola");
    assertThat(response.getLanguageSelection()).isEqualTo(LanguageSelection.DE_ES);

    // Verify other fields are NOT in lean response (compile-time check)
    // response.getExampleSentence() would not compile
    // response.getAudioUrl() would not compile
    // response.getTags() would not compile
  }
}
