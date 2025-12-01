package com.anki.simple.tag;

import com.anki.simple.exception.TagAlreadyExistsException;
import com.anki.simple.exception.TagNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.dto.TagRequest;
import com.anki.simple.tag.dto.TagResponse;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("TagService Integration Tests")
class TagServiceTest {

  @Autowired
  private TagService tagService;

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private UserRepository userRepository;

  private User user;
  private User otherUser;
  private TagRequest tagRequest;

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

    // Create other user for authorization tests
    otherUser = new User();
    otherUser.setUsername("otheruser");
    otherUser.setEmail("other@example.com");
    otherUser.setPassword("encodedPassword");
    otherUser = userRepository.save(otherUser);

    // Create test tag request
    tagRequest = new TagRequest();
    tagRequest.setName("Greetings");
    tagRequest.setColor("#FF0000");
  }

  @Test
  @DisplayName("Given valid request, when create tag, then should create and return tag")
  void givenValidRequest_whenCreateTag_thenShouldCreateAndReturnTag() {
    // When
    TagResponse response = tagService.createTag(tagRequest, user.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isNotNull();
    assertThat(response.getName()).isEqualTo("Greetings");
    assertThat(response.getColor()).isEqualTo("#FF0000");

    // Verify tag is saved in database
    assertThat(tagRepository.findById(response.getId())).isPresent();
  }

  @Test
  @DisplayName("Given nonexistent user, when create tag, then should throw UserNotFoundException")
  void givenNonexistentUser_whenCreateTag_thenShouldThrowUserNotFoundException() {
    // When & Then
    assertThatThrownBy(() -> tagService.createTag(tagRequest, "nonexistentuser"))
      .isInstanceOf(UserNotFoundException.class)
      .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("Given duplicate tag name, when create tag, then should throw TagAlreadyExistsException")
  void givenDuplicateTagName_whenCreateTag_thenShouldThrowTagAlreadyExistsException() {
    // Given
    tagService.createTag(tagRequest, user.getUsername());

    // When & Then
    assertThatThrownBy(() -> tagService.createTag(tagRequest, user.getUsername()))
      .isInstanceOf(TagAlreadyExistsException.class)
      .hasMessageContaining("Greetings");
  }

  @Test
  @DisplayName("Given tag with same name for different user, when create tag, then should succeed")
  void givenTagWithSameNameForDifferentUser_whenCreateTag_thenShouldSucceed() {
    // Given
    tagService.createTag(tagRequest, user.getUsername());

    // When
    TagResponse response = tagService.createTag(tagRequest, otherUser.getUsername());

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo("Greetings");

    // Verify both tags exist in database
    List<Tag> userTags = tagRepository.findByUserId(user.getId());
    List<Tag> otherUserTags = tagRepository.findByUserId(otherUser.getId());
    assertThat(userTags).hasSize(1);
    assertThat(otherUserTags).hasSize(1);
  }

  @Test
  @DisplayName("Given user with tags, when get all tags, then should return all user's tags")
  void givenUserWithTags_whenGetAllTags_thenShouldReturnAllUserTags() {
    // Given
    TagRequest tag1 = new TagRequest();
    tag1.setName("Greetings");
    tag1.setColor("#FF0000");
    tagService.createTag(tag1, user.getUsername());

    TagRequest tag2 = new TagRequest();
    tag2.setName("Verbs");
    tag2.setColor("#00FF00");
    tagService.createTag(tag2, user.getUsername());

    TagRequest tag3 = new TagRequest();
    tag3.setName("Nouns");
    tag3.setColor("#0000FF");
    tagService.createTag(tag3, user.getUsername());

    // Create tag for other user (should not be returned)
    TagRequest otherTag = new TagRequest();
    otherTag.setName("Other");
    otherTag.setColor("#FFFF00");
    tagService.createTag(otherTag, otherUser.getUsername());

    // When
    List<TagResponse> tags = tagService.getAllTags(user.getUsername());

    // Then
    assertThat(tags).hasSize(3);
    assertThat(tags).extracting(TagResponse::getName)
      .containsExactlyInAnyOrder("Greetings", "Verbs", "Nouns");
  }

  @Test
  @DisplayName("Given user with no tags, when get all tags, then should return empty list")
  void givenUserWithNoTags_whenGetAllTags_thenShouldReturnEmptyList() {
    // When
    List<TagResponse> tags = tagService.getAllTags(user.getUsername());

    // Then
    assertThat(tags).isEmpty();
  }

  @Test
  @DisplayName("Given nonexistent user, when get all tags, then should throw UserNotFoundException")
  void givenNonexistentUser_whenGetAllTags_thenShouldThrowUserNotFoundException() {
    // When & Then
    assertThatThrownBy(() -> tagService.getAllTags("nonexistentuser"))
      .isInstanceOf(UserNotFoundException.class)
      .hasMessageContaining("User not found");
  }

  @Test
  @DisplayName("Given valid tag, when delete tag, then should delete tag")
  void givenValidTag_whenDeleteTag_thenShouldDeleteTag() {
    // Given
    TagResponse createdTag = tagService.createTag(tagRequest, user.getUsername());

    // When
    tagService.deleteTag(createdTag.getId(), user.getUsername());

    // Then
    assertThat(tagRepository.findById(createdTag.getId())).isEmpty();
  }

  @Test
  @DisplayName("Given nonexistent tag, when delete tag, then should throw TagNotFoundException")
  void givenNonexistentTag_whenDeleteTag_thenShouldThrowTagNotFoundException() {
    // When & Then
    assertThatThrownBy(() -> tagService.deleteTag(999L, user.getUsername()))
      .isInstanceOf(TagNotFoundException.class)
      .hasMessageContaining("999");
  }

  @Test
  @DisplayName("Given tag owned by other user, when delete tag, then should throw UnauthorizedException")
  void givenTagOwnedByOtherUser_whenDeleteTag_thenShouldThrowUnauthorizedException() {
    // Given
    TagResponse createdTag = tagService.createTag(tagRequest, user.getUsername());

    // When & Then
    assertThatThrownBy(() -> tagService.deleteTag(createdTag.getId(), otherUser.getUsername()))
      .isInstanceOf(UnauthorizedException.class)
      .hasMessageContaining("Unauthorized access to tag");
  }

  @Test
  @DisplayName("Given nonexistent user, when delete tag, then should throw UserNotFoundException")
  void givenNonexistentUser_whenDeleteTag_thenShouldThrowUserNotFoundException() {
    // Given
    TagResponse createdTag = tagService.createTag(tagRequest, user.getUsername());

    // When & Then
    assertThatThrownBy(() -> tagService.deleteTag(createdTag.getId(), "nonexistentuser"))
      .isInstanceOf(UserNotFoundException.class)
      .hasMessageContaining("User not found");
  }
}
