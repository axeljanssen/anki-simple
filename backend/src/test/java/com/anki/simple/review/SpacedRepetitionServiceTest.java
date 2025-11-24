package com.anki.simple.review;

import com.anki.simple.vocabulary.VocabularyCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("SpacedRepetitionService Tests")
class SpacedRepetitionServiceTest {

  private SpacedRepetitionService spacedRepetitionService;

  @BeforeEach
  void setUp() {
    spacedRepetitionService = new SpacedRepetitionService();
  }

  @Test
  @DisplayName("Given new card with quality < 3, when update schedule, then should reset to 1 day interval")
  void givenNewCardWithPoorQuality_whenUpdateSchedule_thenShouldResetToOneDay() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(0);
    card.setRepetitions(0);
    int quality = 2;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getIntervalDays()).isEqualTo(1);
    assertThat(card.getRepetitions()).isEqualTo(0);
    assertThat(card.getLastReviewed()).isNotNull();
    assertThat(card.getNextReview()).isAfter(LocalDateTime.now());
    assertThat(card.getEaseFactor()).isLessThan(2.5);
  }

  @Test
  @DisplayName("Given new card with quality >= 3, when first review, then should set 1 day interval")
  void givenNewCardWithGoodQuality_whenFirstReview_thenShouldSetOneDayInterval() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(0);
    card.setRepetitions(0);
    int quality = 4;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getIntervalDays()).isEqualTo(1);
    assertThat(card.getRepetitions()).isEqualTo(1);
    assertThat(card.getLastReviewed()).isNotNull();
    assertThat(card.getNextReview()).isAfter(LocalDateTime.now());
  }

  @Test
  @DisplayName("Given card with 1 repetition and quality >= 3, when second review, then should set 6 day interval")
  void givenCardWithOneRepetition_whenSecondReview_thenShouldSetSixDayInterval() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(1);
    int quality = 4;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getIntervalDays()).isEqualTo(6);
    assertThat(card.getRepetitions()).isEqualTo(2);
    assertThat(card.getLastReviewed()).isNotNull();
    assertThat(card.getNextReview()).isAfter(LocalDateTime.now());
  }

  @Test
  @DisplayName("Given card with 2+ repetitions and quality >= 3, when review, then should multiply interval by ease factor")
  void givenCardWithMultipleRepetitions_whenReview_thenShouldMultiplyByEaseFactor() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(6);
    card.setRepetitions(2);
    int quality = 4;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getIntervalDays()).isEqualTo(15); // 6 * 2.5 = 15
    assertThat(card.getRepetitions()).isEqualTo(3);
    assertThat(card.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
  }

  @Test
  @DisplayName("Given quality 5 (perfect), when update schedule, then should increase ease factor")
  void givenPerfectQuality_whenUpdateSchedule_thenShouldIncreaseEaseFactor() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(6);
    card.setRepetitions(2);
    int quality = 5;
    double initialEaseFactor = card.getEaseFactor();

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getEaseFactor()).isGreaterThan(initialEaseFactor);
    assertThat(card.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
  }

  @Test
  @DisplayName("Given quality 3 (correct with difficulty), when update schedule, then ease factor should decrease slightly")
  void givenQualityThree_whenUpdateSchedule_thenEaseFactorShouldDecreaseSlightly() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(6);
    card.setRepetitions(2);
    int quality = 3;
    double initialEaseFactor = card.getEaseFactor();

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getEaseFactor()).isLessThan(initialEaseFactor);
    assertThat(card.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
  }

  @Test
  @DisplayName("Given ease factor would go below 1.3, when update schedule, then should be clamped to 1.3")
  void givenEaseFactorWouldGoBelowMinimum_whenUpdateSchedule_thenShouldClampToMinimum() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(1.3);
    card.setIntervalDays(1);
    card.setRepetitions(1);
    int quality = 0; // Complete blackout

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getEaseFactor()).isEqualTo(1.3);
    assertThat(card.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
  }

  @Test
  @DisplayName("Given quality < 3, when update schedule multiple times, then should stay at 1 day and reset repetitions")
  void givenPoorQualityMultipleTimes_whenUpdateSchedule_thenShouldKeepResetting() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(15);
    card.setRepetitions(5);
    int quality = 1;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getIntervalDays()).isEqualTo(1);
    assertThat(card.getRepetitions()).isEqualTo(0);

    // When - review again with poor quality
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then - should still be reset
    assertThat(card.getIntervalDays()).isEqualTo(1);
    assertThat(card.getRepetitions()).isEqualTo(0);
  }

  @Test
  @DisplayName("Given quality out of range (< 0), when update schedule, then should throw IllegalArgumentException")
  void givenQualityBelowZero_whenUpdateSchedule_thenShouldThrowException() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(0);
    int quality = -1;

    // When & Then
    assertThatThrownBy(() -> spacedRepetitionService.updateCardSchedule(card, quality))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Quality must be between 0 and 5");
  }

  @Test
  @DisplayName("Given quality out of range (> 5), when update schedule, then should throw IllegalArgumentException")
  void givenQualityAboveFive_whenUpdateSchedule_thenShouldThrowException() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(0);
    int quality = 6;

    // When & Then
    assertThatThrownBy(() -> spacedRepetitionService.updateCardSchedule(card, quality))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Quality must be between 0 and 5");
  }

  @Test
  @DisplayName("Given card reviewed, when update schedule, then nextReview should be after lastReviewed")
  void givenCardReviewed_whenUpdateSchedule_thenNextReviewShouldBeAfterLastReviewed() {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(1);
    int quality = 4;

    // When
    spacedRepetitionService.updateCardSchedule(card, quality);

    // Then
    assertThat(card.getLastReviewed()).isNotNull();
    assertThat(card.getNextReview()).isNotNull();
    assertThat(card.getNextReview()).isAfter(card.getLastReviewed());
  }

  @Test
  @DisplayName("Given card with all quality levels 0-5, when update schedule, then should handle all cases")
  void givenAllQualityLevels_whenUpdateSchedule_thenShouldHandleAllCases() {
    for (int quality = 0; quality <= 5; quality++) {
      // Given
      VocabularyCard card = new VocabularyCard();
      card.setEaseFactor(2.5);
      card.setIntervalDays(1);
      card.setRepetitions(1);

      // When
      spacedRepetitionService.updateCardSchedule(card, quality);

      // Then
      assertThat(card.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
      assertThat(card.getIntervalDays()).isGreaterThan(0);
      assertThat(card.getLastReviewed()).isNotNull();
      assertThat(card.getNextReview()).isNotNull();

      if (quality < 3) {
        assertThat(card.getIntervalDays()).isEqualTo(1);
        assertThat(card.getRepetitions()).isEqualTo(0);
      } else {
        assertThat(card.getRepetitions()).isGreaterThan(0);
      }
    }
  }
}
