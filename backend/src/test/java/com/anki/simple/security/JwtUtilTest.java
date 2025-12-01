package com.anki.simple.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    // Set test values using reflection
    ReflectionTestUtils.setField(jwtUtil, "secret", "testsecrettestsecrettestsecrettestsecret");
    ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours

    userDetails = new User("testuser", "password", new ArrayList<>());
  }

  @Test
  @DisplayName("Given username, when generate token, then should create valid token")
  void givenUsername_whenGenerateToken_thenShouldCreateValidToken() {
    // When
    String token = jwtUtil.generateToken("testuser");

    // Then
    assertThat(token).isNotNull();
    assertThat(token).isNotEmpty();
    assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
  }

  @Test
  @DisplayName("Given token, when extract username, then should return correct username")
  void givenToken_whenExtractUsername_thenShouldReturnCorrectUsername() {
    // Given
    String token = jwtUtil.generateToken("testuser");

    // When
    String username = jwtUtil.extractUsername(token);

    // Then
    assertThat(username).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Given token, when extract expiration, then should return future date")
  void givenToken_whenExtractExpiration_thenShouldReturnFutureDate() {
    // Given
    String token = jwtUtil.generateToken("testuser");

    // When
    Date expiration = jwtUtil.extractExpiration(token);

    // Then
    assertThat(expiration).isAfter(new Date());
  }

  @Test
  @DisplayName("Given valid token, when validate token, then should return true")
  void givenValidToken_whenValidateToken_thenShouldReturnTrue() {
    // Given
    String token = jwtUtil.generateToken("testuser");

    // When
    Boolean isValid = jwtUtil.validateToken(token, userDetails);

    // Then
    assertThat(isValid).isTrue();
  }

  @Test
  @DisplayName("Given token with wrong username, when validate token, then should return false")
  void givenTokenWithWrongUsername_whenValidateToken_thenShouldReturnFalse() {
    // Given
    String token = jwtUtil.generateToken("differentuser");

    // When
    Boolean isValid = jwtUtil.validateToken(token, userDetails);

    // Then
    assertThat(isValid).isFalse();
  }

}
