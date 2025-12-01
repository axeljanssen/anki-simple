package com.anki.simple.vocabulary;

import com.anki.simple.tag.Tag;
import com.anki.simple.tag.TagRepository;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("VocabularyController Integration Tests")
class VocabularyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private VocabularyRepository vocabularyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private User user;

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
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given valid request, when create card, then should return 200")
  void givenValidRequest_whenCreateCard_thenShouldReturn200() throws Exception {
    // Given
    String requestJson = "{\"front\":\"Hello\",\"back\":\"Hola\",\"languageSelection\":\"DE_ES\"}";

    // When & Then
    mockMvc.perform(post("/api/vocabulary")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.front").value("Hello"))
      .andExpect(jsonPath("$.back").value("Hola"))
      .andExpect(jsonPath("$.languageSelection").value("DE_ES"));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given user with cards, when get all cards, then should return all cards")
  void givenUserWithCards_whenGetAllCards_thenShouldReturnAllCards() throws Exception {
    // Given
    VocabularyCard card1 = new VocabularyCard();
    card1.setFront("Hello");
    card1.setBack("Hola");
    card1.setLanguageSelection(LanguageSelection.DE_ES);
    card1.setUser(user);
    vocabularyRepository.save(card1);

    VocabularyCard card2 = new VocabularyCard();
    card2.setFront("Goodbye");
    card2.setBack("Adiós");
    card2.setLanguageSelection(LanguageSelection.DE_FR);
    card2.setUser(user);
    vocabularyRepository.save(card2);

    // When & Then
    mockMvc.perform(get("/api/vocabulary"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given card, when get due cards, then should return due cards")
  void givenCard_whenGetDueCards_thenShouldReturnDueCards() throws Exception {
    // Given
    VocabularyCard dueCard = new VocabularyCard();
    dueCard.setFront("Hello");
    dueCard.setBack("Hola");
    dueCard.setLanguageSelection(LanguageSelection.DE_ES);
    dueCard.setUser(user);
    dueCard.setNextReview(LocalDateTime.now().minusDays(1));
    vocabularyRepository.save(dueCard);

    // When & Then
    mockMvc.perform(get("/api/vocabulary/due"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given card, when get due count, then should return count")
  void givenCard_whenGetDueCount_thenShouldReturnCount() throws Exception {
    // Given
    VocabularyCard dueCard = new VocabularyCard();
    dueCard.setFront("Hello");
    dueCard.setBack("Hola");
    dueCard.setLanguageSelection(LanguageSelection.DE_ES);
    dueCard.setUser(user);
    dueCard.setNextReview(LocalDateTime.now().minusDays(1));
    vocabularyRepository.save(dueCard);

    // When & Then
    mockMvc.perform(get("/api/vocabulary/due/count"))
      .andExpect(status().isOk())
      .andExpect(content().string("1"));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given valid request, when update card, then should return 200")
  void givenValidRequest_whenUpdateCard_thenShouldReturn200() throws Exception {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setFront("Hello");
    card.setBack("Hola");
    card.setLanguageSelection(LanguageSelection.DE_ES);
    card.setUser(user);
    card = vocabularyRepository.save(card);

    String requestJson = "{\"front\":\"Hi\",\"back\":\"Hola\",\"languageSelection\":\"DE_ES\"}";

    // When & Then
    mockMvc.perform(put("/api/vocabulary/" + card.getId())
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.front").value("Hi"));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given card, when delete card, then should return 204")
  void givenCard_whenDeleteCard_thenShouldReturn204() throws Exception {
    // Given
    VocabularyCard card = new VocabularyCard();
    card.setFront("Hello");
    card.setBack("Hola");
    card.setLanguageSelection(LanguageSelection.DE_ES);
    card.setUser(user);
    card = vocabularyRepository.save(card);

    // When & Then
    mockMvc.perform(delete("/api/vocabulary/" + card.getId())
        .with(csrf()))
      .andExpect(status().isNoContent());
  }
}
