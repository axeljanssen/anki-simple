package com.anki.simple.security;

import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("CustomUserDetailsService Integration Tests")
class CustomUserDetailsServiceTest {

  @Autowired
  private CustomUserDetailsService customUserDetailsService;

  @Autowired
  private UserRepository userRepository;

  private User user;

  @BeforeEach
  void setUp() {
    // Clean up
    userRepository.deleteAll();

    // Create test user
    user = new User();
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
    user = userRepository.save(user);
  }

  @Test
  @DisplayName("Given existing username, when load user by username, then should return user details")
  void givenExistingUsername_whenLoadUserByUsername_thenShouldReturnUserDetails() {
    // When
    UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

    // Then
    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo("testuser");
    assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
    assertThat(userDetails.getAuthorities()).isEmpty();
  }

  @Test
  @DisplayName("Given nonexistent username, when load user by username, then should throw UsernameNotFoundException")
  void givenNonexistentUsername_whenLoadUserByUsername_thenShouldThrowUsernameNotFoundException() {
    // When & Then
    assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistentuser"))
      .isInstanceOf(UsernameNotFoundException.class)
      .hasMessageContaining("User not found: nonexistentuser");
  }
}
