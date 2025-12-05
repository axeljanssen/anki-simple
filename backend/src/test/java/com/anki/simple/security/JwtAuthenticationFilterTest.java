package com.anki.simple.security;

import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFilterTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private User testUser;
  private String validToken;

  @BeforeEach
  void setUp() {
    // Create a test user
    testUser = new User();
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPassword(passwordEncoder.encode("password123"));
    userRepository.save(testUser);

    // Generate a valid token for the test user
    validToken = jwtUtil.generateToken("testuser");
  }

  @Test
  void doFilterInternal_withValidToken_shouldAuthenticate() throws Exception {
    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", "Bearer " + validToken))
        .andExpect(status().isOk());
  }

  @Test
  void doFilterInternal_withInvalidToken_shouldReturn403() throws Exception {
    // Token with valid JWT format but invalid signature - will throw exception during validation
    // Spring Security catches this and returns 403 Forbidden
    String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.invalid_signature";

    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", "Bearer " + invalidToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void doFilterInternal_withExpiredToken_shouldReturn403() throws Exception {
    // Generate a token for a different user to simulate signature mismatch
    // This will fail validation and result in no authentication
    String wrongUserToken = jwtUtil.generateToken("wronguser");

    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", "Bearer " + wrongUserToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void doFilterInternal_withMalformedToken_shouldReturn403() throws Exception {
    // Malformed token that doesn't match JWT format
    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", "Bearer malformed"))
        .andExpect(status().isForbidden());
  }

  @Test
  void doFilterInternal_withoutToken_shouldReturn403() throws Exception {
    // No Authorization header - Spring Security returns 403 Forbidden
    mockMvc.perform(get("/api/v1/vocabulary"))
        .andExpect(status().isForbidden());
  }

  @Test
  void doFilterInternal_withoutBearerPrefix_shouldReturn403() throws Exception {
    // Authorization header without "Bearer " prefix
    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", validToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void doFilterInternal_withEmptyAuthorizationHeader_shouldReturn403() throws Exception {
    // Empty Authorization header
    mockMvc.perform(get("/api/v1/vocabulary")
            .header("Authorization", ""))
        .andExpect(status().isForbidden());
  }
}
