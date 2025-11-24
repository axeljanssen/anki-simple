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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocabularyService Tests")
class VocabularyServiceTest {

  @Mock
  private VocabularyRepository vocabularyRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private TagRepository tagRepository;

  @InjectMocks
  private VocabularyService vocabularyService;

  private User user;
  private VocabularyCard card;
  private VocabularyCardRequest request;
  private Tag tag;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");

    card = new VocabularyCard();
    card.setId(1L);
    card.setFront("Hello");
    card.setBack("Hola");
    card.setExampleSentence("Hello, how are you?");
    card.setSourceLanguage("English");
    card.setTargetLanguage("Spanish");
    card.setUser(user);
    card.setTags(new HashSet<>());
    card.setEaseFactor(2.5);
    card.setIntervalDays(1);
    card.setRepetitions(0);
    card.setCreatedAt(LocalDateTime.now());

    request = new VocabularyCardRequest();
    request.setFront("Hello");
    request.setBack("Hola");
    request.setExampleSentence("Hello, how are you?");
    request.setSourceLanguage("English");
    request.setTargetLanguage("Spanish");

    tag = new Tag();
    tag.setId(1L);
    tag.setName("Greetings");
    tag.setColor("#FF0000");
  }

  @Test
  @DisplayName("Given valid request, when create card, then should create and return card")
  void givenValidRequest_whenCreateCard_thenShouldCreateAndReturnCard() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.save(any(VocabularyCard.class))).thenReturn(card);

    // When
    VocabularyCardResponse response = vocabularyService.createCard(request, "testuser");

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getFront()).isEqualTo("Hello");
    assertThat(response.getBack()).isEqualTo("Hola");
    assertThat(response.getExampleSentence()).isEqualTo("Hello, how are you?");
    assertThat(response.getSourceLanguage()).isEqualTo("English");
    assertThat(response.getTargetLanguage()).isEqualTo("Spanish");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given user not found, when create card, then should throw UserNotFoundException")
  void givenUserNotFound_whenCreateCard_thenShouldThrowException() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> vocabularyService.createCard(request, "testuser"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository, never()).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given request with tags, when create card, then should include tags")
  void givenRequestWithTags_whenCreateCard_thenShouldIncludeTags() {
    // Given
    request.setTagIds(new HashSet<>(Arrays.asList(1L)));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(tagRepository.findAllById(any())).thenReturn(Arrays.asList(tag));
    when(vocabularyRepository.save(any(VocabularyCard.class))).thenReturn(card);

    // When
    VocabularyCardResponse response = vocabularyService.createCard(request, "testuser");

    // Then
    assertThat(response).isNotNull();
    verify(tagRepository).findAllById(any());
    verify(vocabularyRepository).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given username, when get all cards, then should return user's cards")
  void givenUsername_whenGetAllCards_thenShouldReturnUsersCards() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findByUserId(anyLong())).thenReturn(Arrays.asList(card));

    // When
    List<VocabularyCardResponse> responses = vocabularyService.getAllCards("testuser");

    // Then
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getFront()).isEqualTo("Hello");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findByUserId(1L);
  }

  @Test
  @DisplayName("Given user not found, when get all cards, then should throw UserNotFoundException")
  void givenUserNotFound_whenGetAllCards_thenShouldThrowException() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> vocabularyService.getAllCards("testuser"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository, never()).findByUserId(anyLong());
  }

  @Test
  @DisplayName("Given username, when get due cards, then should return due cards")
  void givenUsername_whenGetDueCards_thenShouldReturnDueCards() {
    // Given
    card.setNextReview(LocalDateTime.now().minusDays(1));
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findDueCards(anyLong(), any(LocalDateTime.class)))
        .thenReturn(Arrays.asList(card));

    // When
    List<VocabularyCardResponse> responses = vocabularyService.getDueCards("testuser");

    // Then
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0).getFront()).isEqualTo("Hello");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findDueCards(eq(1L), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("Given username, when get due cards count, then should return count")
  void givenUsername_whenGetDueCardsCount_thenShouldReturnCount() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.countByUserIdAndNextReviewBefore(anyLong(), any(LocalDateTime.class)))
        .thenReturn(5L);

    // When
    long count = vocabularyService.getDueCardsCount("testuser");

    // Then
    assertThat(count).isEqualTo(5L);

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).countByUserIdAndNextReviewBefore(eq(1L), any(LocalDateTime.class));
  }

  @Test
  @DisplayName("Given valid update request, when update card, then should update and return card")
  void givenValidUpdateRequest_whenUpdateCard_thenShouldUpdateAndReturnCard() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.of(card));
    when(vocabularyRepository.save(any(VocabularyCard.class))).thenReturn(card);

    // When
    VocabularyCardResponse response = vocabularyService.updateCard(1L, request, "testuser");

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getFront()).isEqualTo("Hello");

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given card not found, when update card, then should throw CardNotFoundException")
  void givenCardNotFound_whenUpdateCard_thenShouldThrowException() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> vocabularyService.updateCard(1L, request, "testuser"))
        .isInstanceOf(CardNotFoundException.class);

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository, never()).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given unauthorized user, when update card, then should throw UnauthorizedException")
  void givenUnauthorizedUser_whenUpdateCard_thenShouldThrowException() {
    // Given
    User differentUser = new User();
    differentUser.setId(2L);
    differentUser.setUsername("otheruser");

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(differentUser));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.of(card));

    // When & Then
    assertThatThrownBy(() -> vocabularyService.updateCard(1L, request, "otheruser"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Unauthorized");

    verify(userRepository).findByUsername("otheruser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository, never()).save(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given valid card id, when delete card, then should delete card")
  void givenValidCardId_whenDeleteCard_thenShouldDeleteCard() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.of(card));

    // When
    vocabularyService.deleteCard(1L, "testuser");

    // Then
    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository).delete(card);
  }

  @Test
  @DisplayName("Given card not found, when delete card, then should throw CardNotFoundException")
  void givenCardNotFound_whenDeleteCard_thenShouldThrowException() {
    // Given
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> vocabularyService.deleteCard(1L, "testuser"))
        .isInstanceOf(CardNotFoundException.class);

    verify(userRepository).findByUsername("testuser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository, never()).delete(any(VocabularyCard.class));
  }

  @Test
  @DisplayName("Given unauthorized user, when delete card, then should throw UnauthorizedException")
  void givenUnauthorizedUser_whenDeleteCard_thenShouldThrowException() {
    // Given
    User differentUser = new User();
    differentUser.setId(2L);
    differentUser.setUsername("otheruser");

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(differentUser));
    when(vocabularyRepository.findById(anyLong())).thenReturn(Optional.of(card));

    // When & Then
    assertThatThrownBy(() -> vocabularyService.deleteCard(1L, "otheruser"))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessageContaining("Unauthorized");

    verify(userRepository).findByUsername("otheruser");
    verify(vocabularyRepository).findById(1L);
    verify(vocabularyRepository, never()).delete(any(VocabularyCard.class));
  }
}
