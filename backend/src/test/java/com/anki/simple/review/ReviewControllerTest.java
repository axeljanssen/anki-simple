package com.anki.simple.review;

import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.LanguageSelection;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.VocabularyRepository;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("ReviewController Integration Tests")
class ReviewControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private VocabularyRepository vocabularyRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewHistoryRepository reviewHistoryRepository;

  private User user;
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
  @WithMockUser(username = "testuser")
  @DisplayName("Given valid review request, when review card, then should return 200")
  void givenValidReviewRequest_whenReviewCard_thenShouldReturn200() throws Exception {
    // Given
    String requestJson = "{\"cardId\":" + card.getId() + ",\"quality\":4}";

    // When & Then
    mockMvc.perform(post("/api/review")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(card.getId()))
      .andExpect(jsonPath("$.repetitions").value(1));
  }
}
