package com.anki.simple.review.mapper;

import com.anki.simple.review.ReviewHistory;
import com.anki.simple.user.User;
import com.anki.simple.vocabulary.LanguageSelection;
import com.anki.simple.vocabulary.VocabularyCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ReviewHistoryMapper Tests")
class ReviewHistoryMapperTest {

  @Autowired
  private ReviewHistoryMapper reviewHistoryMapper;

  private VocabularyCard card;

  @BeforeEach
  void setUp() {
    User user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("password");

    card = new VocabularyCard();
    card.setId(1L);
    card.setFront("Hello");
    card.setBack("Hola");
    card.setLanguageSelection(LanguageSelection.DE_ES);
    card.setEaseFactor(2.5);
    card.setIntervalDays(3);
    card.setRepetitions(2);
    card.setUser(user);
  }

  @Test
  @DisplayName("Given card and quality, when create from card and quality, then should create review history")
  void givenCardAndQuality_whenCreateFromCardAndQuality_thenShouldCreateReviewHistory() {
    // Given
    int quality = 4;

    // When
    ReviewHistory history = reviewHistoryMapper.createFromCardAndQuality(card, quality);

    // Then
    assertThat(history).isNotNull();
    assertThat(history.getCard()).isEqualTo(card);
    assertThat(history.getQuality()).isEqualTo(4);
    assertThat(history.getEaseFactor()).isEqualTo(2.5);
    assertThat(history.getIntervalDays()).isEqualTo(3);
    assertThat(history.getReviewedAt()).isNull(); // Set by entity
  }

  @Test
  @DisplayName("Given card with different ease factor, when create history, then should use card's ease factor")
  void givenCardWithDifferentEaseFactor_whenCreateHistory_thenShouldUseCardEaseFactor() {
    // Given
    card.setEaseFactor(1.8);
    card.setIntervalDays(10);
    int quality = 5;

    // When
    ReviewHistory history = reviewHistoryMapper.createFromCardAndQuality(card, quality);

    // Then
    assertThat(history.getEaseFactor()).isEqualTo(1.8);
    assertThat(history.getIntervalDays()).isEqualTo(10);
  }

  @Test
  @DisplayName("Given quality zero, when create history, then should create history with quality zero")
  void givenQualityZero_whenCreateHistory_thenShouldCreateHistoryWithQualityZero() {
    // Given
    int quality = 0;

    // When
    ReviewHistory history = reviewHistoryMapper.createFromCardAndQuality(card, quality);

    // Then
    assertThat(history.getQuality()).isEqualTo(0);
  }

  @Test
  @DisplayName("Given quality five, when create history, then should create history with quality five")
  void givenQualityFive_whenCreateHistory_thenShouldCreateHistoryWithQualityFive() {
    // Given
    int quality = 5;

    // When
    ReviewHistory history = reviewHistoryMapper.createFromCardAndQuality(card, quality);

    // Then
    assertThat(history.getQuality()).isEqualTo(5);
  }
}
