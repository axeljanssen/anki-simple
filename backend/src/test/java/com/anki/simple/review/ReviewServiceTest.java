package com.anki.simple.review;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.review.dto.ReviewRequest;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.LanguageSelection;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.VocabularyRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("ReviewService Integration Tests")
class ReviewServiceTest {

  @Autowired
  private ReviewService reviewService;

  @Autowired
  private VocabularyRepository vocabularyRepository;

  @Autowired
  private ReviewHistoryRepository reviewHistoryRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private User otherUser;
  private VocabularyCard card;

  @BeforeEach
  void setUp() {
    // Clean up
    reviewHistoryRepository.deleteAll();
    vocabularyRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user = userRepository.save(user);

    // Create other user for authorization tests
    otherUser = new User();
    otherUser.setUsername("otheruser");
    otherUser.setEmail("other@example.com");
    otherUser.setPassword("encodedPassword");
    otherUser = userRepository.save(otherUser);

    // Create test card
    card = new VocabularyCard();
    card.setFront("Hello");
    card.setBack("Hola");
    card.setLanguageSelection(LanguageSelection.DE_ES);
    card.setUser(user);
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(0);
    card.setLastReviewed(LocalDateTime.now().minusDays(2));
    card.setNextReview(LocalDateTime.now().minusDays(1));
    card = vocabularyRepository.save(card);
  }

  @Test
  @DisplayName("Given valid review with quality 4, when review card, then should update card and create history")
  void givenValidReviewWithQuality4_whenReviewCard_thenShouldUpdateCardAndCreateHistory() {
    // Given
    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(4);

    // When
    VocabularyCardResponse response = reviewService.reviewCard(request, user.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(card.getId());
    assertThat(response.getRepetitions()).isGreaterThan(0);
    assertThat(response.getIntervalDays()).isGreaterThanOrEqualTo(1);

    // Verify history was created
    assertThat(reviewHistoryRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given quality 5, when review card, then should advance to next interval")
  void givenQuality5_whenReviewCard_thenShouldAdvanceToNextInterval() {
    // Given
    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(5);

    // When
    VocabularyCardResponse response = reviewService.reviewCard(request, user.getUsername());

    // Then
    assertThat(response.getRepetitions()).isEqualTo(1);
    assertThat(response.getIntervalDays()).isEqualTo(1); // First repetition -> 1 day
  }

  @Test
  @DisplayName("Given quality 2, when review card, then should reset interval")
  void givenQuality2_whenReviewCard_thenShouldResetInterval() {
    // Given
    card.setRepetitions(3);
    card.setIntervalDays(10);
    card = vocabularyRepository.save(card);

    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(2);

    // When
    VocabularyCardResponse response = reviewService.reviewCard(request, user.getUsername());

    // Then
    assertThat(response.getRepetitions()).isEqualTo(0);
    assertThat(response.getIntervalDays()).isEqualTo(1); // Reset to 1 day
  }

  @Test
  @DisplayName("Given nonexistent user, when review card, then should throw UserNotFoundException")
  void givenNonexistentUser_whenReviewCard_thenShouldThrowUserNotFoundException() {
    // Given
    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(4);

    // When & Then
    assertThatThrownBy(() -> reviewService.reviewCard(request, "nonexistentuser"))
      .isInstanceOf(UserNotFoundException.class)
      .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("Given nonexistent card, when review card, then should throw CardNotFoundException")
  void givenNonexistentCard_whenReviewCard_thenShouldThrowCardNotFoundException() {
    // Given
    ReviewRequest request = new ReviewRequest();
    request.setCardId(999L);
    request.setQuality(4);

    // When & Then
    assertThatThrownBy(() -> reviewService.reviewCard(request, user.getUsername()))
      .isInstanceOf(CardNotFoundException.class)
      .hasMessageContaining("999");
  }

  @Test
  @DisplayName("Given card owned by other user, when review card, then should throw UnauthorizedException")
  void givenCardOwnedByOtherUser_whenReviewCard_thenShouldThrowUnauthorizedException() {
    // Given
    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(4);

    // When & Then
    assertThatThrownBy(() -> reviewService.reviewCard(request, otherUser.getUsername()))
      .isInstanceOf(UnauthorizedException.class)
      .hasMessageContaining("Unauthorized access to card");
  }

  @Test
  @DisplayName("Given quality 0, when review card, then should reset card")
  void givenQuality0_whenReviewCard_thenShouldResetCard() {
    // Given
    card.setRepetitions(5);
    card.setIntervalDays(30);
    card = vocabularyRepository.save(card);

    ReviewRequest request = new ReviewRequest();
    request.setCardId(card.getId());
    request.setQuality(0);

    // When
    VocabularyCardResponse response = reviewService.reviewCard(request, user.getUsername());

    // Then
    assertThat(response.getRepetitions()).isEqualTo(0);
    assertThat(response.getIntervalDays()).isEqualTo(1);
  }

  @Test
  @DisplayName("Given multiple reviews, when review card, then should create multiple history entries")
  void givenMultipleReviews_whenReviewCard_thenShouldCreateMultipleHistoryEntries() {
    // Given & When
    ReviewRequest request1 = new ReviewRequest();
    request1.setCardId(card.getId());
    request1.setQuality(4);
    reviewService.reviewCard(request1, user.getUsername());

    ReviewRequest request2 = new ReviewRequest();
    request2.setCardId(card.getId());
    request2.setQuality(5);
    reviewService.reviewCard(request2, user.getUsername());

    // Then
    assertThat(reviewHistoryRepository.count()).isEqualTo(2);
  }
}
