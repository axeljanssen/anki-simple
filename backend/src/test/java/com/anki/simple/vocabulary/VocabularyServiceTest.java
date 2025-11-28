package com.anki.simple.vocabulary;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.Tag;
import com.anki.simple.tag.TagRepository;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("VocabularyService Integration Tests")
class VocabularyServiceTest {

  @Autowired
  private VocabularyService vocabularyService;

  @Autowired
  private VocabularyRepository vocabularyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TagRepository tagRepository;

  private User user;
  private User otherUser;
  private VocabularyCardRequest request;
  private Tag tag;

  @BeforeEach
  void setUp() {
    // Clean up
    vocabularyRepository.deleteAll();
    tagRepository.deleteAll();
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

    // Create test request
    request = new VocabularyCardRequest();
    request.setFront("Hello");
    request.setBack("Hola");
    request.setExampleSentence("Hello, how are you?");
    request.setSourceLanguage("English");
    request.setTargetLanguage("Spanish");

    // Create test tag
    tag = new Tag();
    tag.setName("Greetings");
    tag.setColor("#FF0000");
    tag.setUser(user);
    tag = tagRepository.save(tag);
  }

  @Test
  @DisplayName("Given valid request, when create card, then should create and return card")
  void givenValidRequest_whenCreateCard_thenShouldCreateAndReturnCard() {
    // When
    VocabularyCardResponse response = vocabularyService.createCard(request, user.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getFront()).isEqualTo("Hello");
    assertThat(response.getBack()).isEqualTo("Hola");
    assertThat(response.getExampleSentence()).isEqualTo("Hello, how are you?");
    assertThat(response.getSourceLanguage()).isEqualTo("English");
    assertThat(response.getTargetLanguage()).isEqualTo("Spanish");
    assertThat(response.getEaseFactor()).isEqualTo(2.5);
    assertThat(response.getIntervalDays()).isEqualTo(0);
    assertThat(response.getRepetitions()).isEqualTo(0);

    // Verify card was saved to database
    assertThat(vocabularyRepository.findById(response.getId())).isPresent();
  }

  @Test
  @DisplayName("Given user not found, when create card, then should throw UserNotFoundException")
  void givenUserNotFound_whenCreateCard_thenShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> vocabularyService.createCard(request, "nonexistent"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    // Verify no card was created
    assertThat(vocabularyRepository.count()).isEqualTo(0);
  }

  @Test
  @DisplayName("Given request with tags, when create card, then should include tags")
  void givenRequestWithTags_whenCreateCard_thenShouldIncludeTags() {
    // Given
    request.setTagIds(new HashSet<>(Arrays.asList(tag.getId())));

    // When
    VocabularyCardResponse response = vocabularyService.createCard(request, user.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getTags()).hasSize(1);
    assertThat(response.getTags().iterator().next().getName()).isEqualTo("Greetings");
    assertThat(response.getTags().iterator().next().getColor()).isEqualTo("#FF0000");
  }

  @Test
  @DisplayName("Given username, when get all cards, then should return user's cards")
  void givenUsername_whenGetAllCards_thenShouldReturnUsersCards() {
    // Given - create cards for user
    vocabularyService.createCard(request, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Goodbye");
    request2.setBack("Adiós");
    request2.setSourceLanguage("English");
    request2.setTargetLanguage("Spanish");
    vocabularyService.createCard(request2, user.getUsername());

    // Create card for other user (should not be returned)
    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("Thanks");
    request3.setBack("Gracias");
    request3.setSourceLanguage("English");
    request3.setTargetLanguage("Spanish");
    vocabularyService.createCard(request3, otherUser.getUsername());

    // When
    List<VocabularyCardResponse> responses = vocabularyService.getAllCards(user.getUsername(), null, null, null);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).extracting(VocabularyCardResponse::getFront)
        .containsExactlyInAnyOrder("Hello", "Goodbye");
  }

  @Test
  @DisplayName("Given user not found, when get all cards, then should throw UserNotFoundException")
  void givenUserNotFound_whenGetAllCards_thenShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> vocabularyService.getAllCards("nonexistent", null, null, null))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("Given username, when get due cards, then should return due cards")
  void givenUsername_whenGetDueCards_thenShouldReturnDueCards() {
    // Given - create a card and manually set it as due
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    VocabularyCard card = vocabularyRepository.findById(created.getId()).get();
    card.setNextReview(LocalDateTime.now().minusDays(1));
    vocabularyRepository.save(card);

    // When
    List<VocabularyCardResponse> responses = vocabularyService.getDueCards(user.getUsername());

    // Then
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getFront()).isEqualTo("Hello");
  }

  @Test
  @DisplayName("Given username, when get due cards count, then should return count")
  void givenUsername_whenGetDueCardsCount_thenShouldReturnCount() {
    // Given - create multiple cards and set some as due
    for (int i = 0; i < 5; i++) {
      VocabularyCardRequest req = new VocabularyCardRequest();
      req.setFront("Word" + i);
      req.setBack("Palabra" + i);
      req.setSourceLanguage("English");
      req.setTargetLanguage("Spanish");
      VocabularyCardResponse created = vocabularyService.createCard(req, user.getUsername());

      VocabularyCard card = vocabularyRepository.findById(created.getId()).get();
      if (i < 3) {
        // Set first 3 cards as due (past)
        card.setNextReview(LocalDateTime.now().minusDays(1));
      } else {
        // Set last 2 cards as not due (future)
        card.setNextReview(LocalDateTime.now().plusDays(1));
      }
      vocabularyRepository.save(card);
    }

    // When
    long count = vocabularyService.getDueCardsCount(user.getUsername());

    // Then
    assertThat(count).isEqualTo(3);
  }

  @Test
  @DisplayName("Given valid update request, when update card, then should update and return card")
  void givenValidUpdateRequest_whenUpdateCard_thenShouldUpdateAndReturnCard() {
    // Given - create a card
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // Modify request
    request.setFront("Hi");
    request.setBack("Holi");

    // When
    VocabularyCardResponse updated = vocabularyService.updateCard(created.getId(), request, user.getUsername());

    // Then
    assertThat(updated).isNotNull();
    assertThat(updated.getId()).isEqualTo(created.getId());
    assertThat(updated.getFront()).isEqualTo("Hi");
    assertThat(updated.getBack()).isEqualTo("Holi");

    // Verify in database
    VocabularyCard card = vocabularyRepository.findById(created.getId()).get();
    assertThat(card.getFront()).isEqualTo("Hi");
    assertThat(card.getBack()).isEqualTo("Holi");
  }

  @Test
  @DisplayName("Given card not found, when update card, then should throw CardNotFoundException")
  void givenCardNotFound_whenUpdateCard_thenShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> vocabularyService.updateCard(999L, request, user.getUsername()))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @DisplayName("Given unauthorized user, when update card, then should throw UnauthorizedException")
  void givenUnauthorizedUser_whenUpdateCard_thenShouldThrowException() {
    // Given - create a card for user
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // When & Then - try to update with other user
    assertThatThrownBy(() -> vocabularyService.updateCard(created.getId(), request, otherUser.getUsername()))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Unauthorized");
  }

  @Test
  @DisplayName("Given valid card id, when delete card, then should delete card")
  void givenValidCardId_whenDeleteCard_thenShouldDeleteCard() {
    // Given - create a card
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());
    Long cardId = created.getId();

    // Verify card exists
    assertThat(vocabularyRepository.findById(cardId)).isPresent();

    // When
    vocabularyService.deleteCard(cardId, user.getUsername());

    // Then
    assertThat(vocabularyRepository.findById(cardId)).isEmpty();
  }

  @Test
  @DisplayName("Given card not found, when delete card, then should throw CardNotFoundException")
  void givenCardNotFound_whenDeleteCard_thenShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> vocabularyService.deleteCard(999L, user.getUsername()))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @DisplayName("Given unauthorized user, when delete card, then should throw UnauthorizedException")
  void givenUnauthorizedUser_whenDeleteCard_thenShouldThrowException() {
    // Given - create a card for user
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // When & Then - try to delete with other user
    assertThatThrownBy(() -> vocabularyService.deleteCard(created.getId(), otherUser.getUsername()))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Unauthorized");

    // Verify card still exists
    assertThat(vocabularyRepository.findById(created.getId())).isPresent();
  }
}
