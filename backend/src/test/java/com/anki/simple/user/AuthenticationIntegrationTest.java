package com.anki.simple.user;

import com.anki.simple.user.dto.LoginRequest;
import com.anki.simple.user.dto.SignupRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Given valid signup request, when signup, then should create user and return JWT token")
  void givenValidSignupRequest_whenSignup_thenShouldCreateUserAndReturnToken() throws Exception {
    // Given
    SignupRequest request = new SignupRequest();
    request.setUsername("newuser");
    request.setEmail("newuser@example.com");
    request.setPassword("password123");

    // When & Then
    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(notNullValue()))
        .andExpect(jsonPath("$.username").value("newuser"))
        .andExpect(jsonPath("$.email").value("newuser@example.com"));

    // Verify user was created in database
    assertThat(userRepository.findByUsername("newuser")).isPresent();
  }

  @Test
  @DisplayName("Given duplicate username, when signup, then should return conflict error")
  void givenDuplicateUsername_whenSignup_thenShouldReturnConflict() throws Exception {
    // Given - create first user
    SignupRequest firstRequest = new SignupRequest();
    firstRequest.setUsername("existinguser");
    firstRequest.setEmail("first@example.com");
    firstRequest.setPassword("password123");

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(firstRequest)));

    // When - try to create second user with same username
    SignupRequest duplicateRequest = new SignupRequest();
    duplicateRequest.setUsername("existinguser");
    duplicateRequest.setEmail("second@example.com");
    duplicateRequest.setPassword("password123");

    // Then
    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Username Already Exists"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value(notNullValue()));
  }

  @Test
  @DisplayName("Given duplicate email, when signup, then should return conflict error")
  void givenDuplicateEmail_whenSignup_thenShouldReturnConflict() throws Exception {
    // Given - create first user
    SignupRequest firstRequest = new SignupRequest();
    firstRequest.setUsername("user1");
    firstRequest.setEmail("duplicate@example.com");
    firstRequest.setPassword("password123");

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(firstRequest)));

    // When - try to create second user with same email
    SignupRequest duplicateRequest = new SignupRequest();
    duplicateRequest.setUsername("user2");
    duplicateRequest.setEmail("duplicate@example.com");
    duplicateRequest.setPassword("password123");

    // Then
    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(duplicateRequest)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Email Already Exists"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value(notNullValue()));
  }

  @Test
  @DisplayName("Given registered user, when login with correct credentials, then should return JWT token")
  void givenRegisteredUser_whenLoginWithCorrectCredentials_thenShouldReturnToken() throws Exception {
    // Given - signup user first
    SignupRequest signupRequest = new SignupRequest();
    signupRequest.setUsername("loginuser");
    signupRequest.setEmail("login@example.com");
    signupRequest.setPassword("password123");

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signupRequest)));

    // When - login with correct credentials
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("loginuser");
    loginRequest.setPassword("password123");

    // Then
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(notNullValue()))
        .andExpect(jsonPath("$.username").value("loginuser"))
        .andExpect(jsonPath("$.email").value("login@example.com"));
  }

  @Test
  @DisplayName("Given registered user, when login with incorrect password, then should return unauthorized")
  void givenRegisteredUser_whenLoginWithIncorrectPassword_thenShouldReturnUnauthorized() throws Exception {
    // Given - signup user first
    SignupRequest signupRequest = new SignupRequest();
    signupRequest.setUsername("testuser");
    signupRequest.setEmail("test@example.com");
    signupRequest.setPassword("correctpassword");

    mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(signupRequest)));

    // When - login with incorrect password
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("wrongpassword");

    // Then
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Given non-existent user, when login, then should return unauthorized")
  void givenNonExistentUser_whenLogin_thenShouldReturnUnauthorized() throws Exception {
    // Given - no user created
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("nonexistent");
    loginRequest.setPassword("password123");

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("Given signup and login flow, when complete authentication flow, then should work end-to-end")
  void givenCompleteFlow_whenSignupAndLogin_thenShouldWorkEndToEnd() throws Exception {
    // Given - signup request
    SignupRequest signupRequest = new SignupRequest();
    signupRequest.setUsername("flowuser");
    signupRequest.setEmail("flow@example.com");
    signupRequest.setPassword("password123");

    // When - signup
    String signupResponse = mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(signupRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Then - verify signup response contains token
    assertThat(signupResponse).contains("token");
    assertThat(signupResponse).contains("flowuser");
    assertThat(signupResponse).contains("flow@example.com");

    // When - login with same credentials
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("flowuser");
    loginRequest.setPassword("password123");

    String loginResponse = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    // Then - verify login response contains token
    assertThat(loginResponse).contains("token");
    assertThat(loginResponse).contains("flowuser");
    assertThat(loginResponse).contains("flow@example.com");
  }

  @Test
  @DisplayName("Given invalid request with missing fields, when signup, then should return bad request")
  void givenInvalidRequest_whenSignup_thenShouldReturnBadRequest() throws Exception {
    // Given - request with missing required fields
    SignupRequest invalidRequest = new SignupRequest();
    // Missing username, email, and password

    // When & Then
    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }
}
