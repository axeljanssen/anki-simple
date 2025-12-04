package com.anki.simple.vocabulary;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.Tag;
import com.anki.simple.tag.TagRepository;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardLeanResponse;
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
    request.setLanguageSelection(LanguageSelection.DE_ES);

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
    assertThat(response.getLanguageSelection()).isEqualTo(LanguageSelection.DE_ES);
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
  @DisplayName("Given username, when get all cards, then should return lean responses")
  void givenUsername_whenGetAllCards_thenShouldReturnLeanResponses() {
    // Given - create cards for user
    vocabularyService.createCard(request, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Goodbye");
    request2.setBack("Adiós");
    request2.setLanguageSelection(LanguageSelection.DE_FR);
    vocabularyService.createCard(request2, user.getUsername());

    // Create card for other user (should not be returned)
    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("Thanks");
    request3.setBack("Gracias");
    request3.setLanguageSelection(LanguageSelection.DE_ES);
    vocabularyService.createCard(request3, otherUser.getUsername());

    // When
    List<VocabularyCardLeanResponse> responses = vocabularyService.getAllCards(user.getUsername(), null, null, null);

    // Then
    assertThat(responses).hasSize(2);
    assertThat(responses).extracting(VocabularyCardLeanResponse::getFront)
        .containsExactlyInAnyOrder("Hello", "Goodbye");

    // Verify lean response only has 4 fields
    VocabularyCardLeanResponse firstCard = responses.get(0);
    assertThat(firstCard.getId()).isNotNull();
    assertThat(firstCard.getFront()).isNotNull();
    assertThat(firstCard.getBack()).isNotNull();
    assertThat(firstCard.getLanguageSelection()).isNotNull();
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
      req.setLanguageSelection(LanguageSelection.DE_ES);
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
  @DisplayName("Given user with cards, when get total count, then should return correct count")
  void givenUserWithCards_whenGetTotalCount_thenShouldReturnCorrectCount() {
    // Given - create 2 cards for user
    vocabularyService.createCard(request, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Goodbye");
    request2.setBack("Adiós");
    request2.setLanguageSelection(LanguageSelection.DE_ES);
    vocabularyService.createCard(request2, user.getUsername());

    // When
    long count = vocabularyService.getTotalCount(user.getUsername());

    // Then
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("Given user with no cards, when get total count, then should return zero")
  void givenUserWithNoCards_whenGetTotalCount_thenShouldReturnZero() {
    // Given - user exists but has no cards

    // When
    long count = vocabularyService.getTotalCount(user.getUsername());

    // Then
    assertThat(count).isEqualTo(0);
  }

  @Test
  @DisplayName("Given non-existent user, when get total count, then should throw UserNotFoundException")
  void givenNonExistentUser_whenGetTotalCount_thenShouldThrowException() {
    // When & Then
    assertThatThrownBy(() -> vocabularyService.getTotalCount("nonexistent"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
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

  @Test
  @DisplayName("Given valid card id, when get card, then should return card")
  void givenValidCardId_whenGetCard_thenShouldReturnCard() {
    // Given - create a card
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // When
    VocabularyCardResponse response = vocabularyService.getCard(created.getId(), user.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(created.getId());
    assertThat(response.getFront()).isEqualTo("Hello");
    assertThat(response.getBack()).isEqualTo("Hola");
    assertThat(response.getExampleSentence()).isEqualTo("Hello, how are you?");
  }

  @Test
  @DisplayName("Given card not found, when get card, then should throw CardNotFoundException")
  void givenCardNotFound_whenGetCard_thenShouldThrowException() {
    // Given
    Long nonExistentId = 999L;

    // When & Then
    assertThatThrownBy(() -> vocabularyService.getCard(nonExistentId, user.getUsername()))
        .isInstanceOf(CardNotFoundException.class);
  }

  @Test
  @DisplayName("Given unauthorized user, when get card, then should throw UnauthorizedException")
  void givenUnauthorizedUser_whenGetCard_thenShouldThrowException() {
    // Given - create a card for user
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // When & Then - try to get with other user
    assertThatThrownBy(() -> vocabularyService.getCard(created.getId(), otherUser.getUsername()))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Unauthorized");
  }

  @Test
  @DisplayName("Given user not found, when get card, then should throw UserNotFoundException")
  void givenUserNotFound_whenGetCard_thenShouldThrowException() {
    // Given - create a card
    VocabularyCardResponse created = vocabularyService.createCard(request, user.getUsername());

    // When & Then - try to get with nonexistent user
    assertThatThrownBy(() -> vocabularyService.getCard(created.getId(), "nonexistent"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("Given multiple cards, when getAllCards with sortBy front asc, then should return sorted cards")
  void givenMultipleCards_whenGetAllCardsSortedByFrontAsc_thenShouldReturnSortedCards() {
    // Given - create multiple cards with different front values
    VocabularyCardRequest request1 = new VocabularyCardRequest();
    request1.setFront("Zebra");
    request1.setBack("Cebra");
    request1.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request1, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Apple");
    request2.setBack("Manzana");
    request2.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request2, user.getUsername());

    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("Moon");
    request3.setBack("Luna");
    request3.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request3, user.getUsername());

    // When - get all cards sorted by front ascending
    List<VocabularyCardLeanResponse> result = vocabularyService.getAllCards(user.getUsername(), "front", "asc", null);

    // Then - cards should be in alphabetical order
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getFront()).isEqualTo("Apple");
    assertThat(result.get(1).getFront()).isEqualTo("Moon");
    assertThat(result.get(2).getFront()).isEqualTo("Zebra");
  }

  @Test
  @DisplayName("Given multiple cards, when getAllCards with sortBy front desc, then should return reverse sorted cards")
  void givenMultipleCards_whenGetAllCardsSortedByFrontDesc_thenShouldReturnReverseSortedCards() {
    // Given - create multiple cards with different front values
    VocabularyCardRequest request1 = new VocabularyCardRequest();
    request1.setFront("Zebra");
    request1.setBack("Cebra");
    request1.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request1, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Apple");
    request2.setBack("Manzana");
    request2.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request2, user.getUsername());

    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("Moon");
    request3.setBack("Luna");
    request3.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request3, user.getUsername());

    // When - get all cards sorted by front descending
    List<VocabularyCardLeanResponse> result = vocabularyService.getAllCards(user.getUsername(), "front", "desc", null);

    // Then - cards should be in reverse alphabetical order
    assertThat(result).hasSize(3);
    assertThat(result.get(0).getFront()).isEqualTo("Zebra");
    assertThat(result.get(1).getFront()).isEqualTo("Moon");
    assertThat(result.get(2).getFront()).isEqualTo("Apple");
  }

  @Test
  @DisplayName("Given multiple cards, when getAllCards with searchTerm, then should return filtered cards")
  void givenMultipleCards_whenGetAllCardsWithSearchTerm_thenShouldReturnFilteredCards() {
    // Given - create multiple cards with different content
    VocabularyCardRequest request1 = new VocabularyCardRequest();
    request1.setFront("Hello");
    request1.setBack("Hola");
    request1.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request1, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Goodbye");
    request2.setBack("Adiós");
    request2.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request2, user.getUsername());

    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("World");
    request3.setBack("Mundo");
    request3.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request3, user.getUsername());

    // When - search for "Hello"
    List<VocabularyCardLeanResponse> result = vocabularyService.getAllCards(user.getUsername(), null, null, "Hello");

    // Then - only matching card should be returned
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getFront()).isEqualTo("Hello");
  }

  @Test
  @DisplayName("Given multiple cards, when getAllCards with search and sort, then should return filtered and sorted cards")
  void givenMultipleCards_whenGetAllCardsWithSearchAndSort_thenShouldReturnFilteredAndSortedCards() {
    // Given - create multiple cards
    VocabularyCardRequest request1 = new VocabularyCardRequest();
    request1.setFront("Zoo");
    request1.setBack("Zoológico");
    request1.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request1, user.getUsername());

    VocabularyCardRequest request2 = new VocabularyCardRequest();
    request2.setFront("Zebra");
    request2.setBack("Cebra");
    request2.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request2, user.getUsername());

    VocabularyCardRequest request3 = new VocabularyCardRequest();
    request3.setFront("Apple");
    request3.setBack("Manzana");  // Contains "z" in "Manzana"
    request3.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request3, user.getUsername());

    VocabularyCardRequest request4 = new VocabularyCardRequest();
    request4.setFront("Zone");
    request4.setBack("Zona");
    request4.setLanguageSelection(LanguageSelection.EN_ES);
    vocabularyService.createCard(request4, user.getUsername());

    // When - search for "Z" (case-insensitive) and sort by front ascending
    // Search matches: Zoo (front), Zebra (front), Zone (front, back), Apple (back="Manzana")
    List<VocabularyCardLeanResponse> result = vocabularyService.getAllCards(user.getUsername(), "front", "asc", "Z");

    // Then - all 4 cards containing "Z" or "z" should be returned, sorted alphabetically by front
    assertThat(result).hasSize(4);
    assertThat(result.get(0).getFront()).isEqualTo("Apple");  // back contains "z" in "Manzana"
    assertThat(result.get(1).getFront()).isEqualTo("Zebra");
    assertThat(result.get(2).getFront()).isEqualTo("Zone");
    assertThat(result.get(3).getFront()).isEqualTo("Zoo");
  }
}
