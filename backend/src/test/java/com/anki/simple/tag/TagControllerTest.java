package com.anki.simple.tag;

import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("TagController Integration Tests")
class TagControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp() {
    // Clean up
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
  @DisplayName("Given valid request, when create tag, then should return 200")
  void givenValidRequest_whenCreateTag_thenShouldReturn200() throws Exception {
    // Given
    String requestJson = "{\"name\":\"Greetings\",\"color\":\"#FF0000\"}";

    // When & Then
    mockMvc.perform(post("/api/v1/tags")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestJson))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Greetings"))
      .andExpect(jsonPath("$.color").value("#FF0000"));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given user with tags, when get all tags, then should return all tags")
  void givenUserWithTags_whenGetAllTags_thenShouldReturnAllTags() throws Exception {
    // Given
    Tag tag1 = new Tag();
    tag1.setName("Greetings");
    tag1.setColor("#FF0000");
    tag1.setUser(user);
    tagRepository.save(tag1);

    Tag tag2 = new Tag();
    tag2.setName("Verbs");
    tag2.setColor("#00FF00");
    tag2.setUser(user);
    tagRepository.save(tag2);

    // When & Then
    mockMvc.perform(get("/api/v1/tags"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  @WithMockUser(username = "testuser")
  @DisplayName("Given tag, when delete tag, then should return 204")
  void givenTag_whenDeleteTag_thenShouldReturn204() throws Exception {
    // Given
    Tag tag = new Tag();
    tag.setName("Greetings");
    tag.setColor("#FF0000");
    tag.setUser(user);
    tag = tagRepository.save(tag);

    // When & Then
    mockMvc.perform(delete("/api/v1/tags/" + tag.getId())
        .with(csrf()))
      .andExpect(status().isNoContent());
  }
}
