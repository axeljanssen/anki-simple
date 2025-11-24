package com.anki.simple.user;

import com.anki.simple.exception.EmailAlreadyExistsException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.exception.UsernameAlreadyExistsException;
import com.anki.simple.security.JwtUtil;
import com.anki.simple.user.dto.AuthResponse;
import com.anki.simple.user.dto.LoginRequest;
import com.anki.simple.user.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private AuthenticationManager authenticationManager;

  @InjectMocks
  private UserService userService;

  private SignupRequest signupRequest;
  private LoginRequest loginRequest;
  private User user;

  @BeforeEach
  void setUp() {
    signupRequest = new SignupRequest();
    signupRequest.setUsername("testuser");
    signupRequest.setEmail("test@example.com");
    signupRequest.setPassword("password123");

    loginRequest = new LoginRequest();
    loginRequest.setUsername("testuser");
    loginRequest.setPassword("password123");

    user = new User();
    user.setId(1L);
    user.setUsername("testuser");
    user.setEmail("test@example.com");
    user.setPassword("encodedPassword");
  }

  @Test
  @DisplayName("Given valid signup request, when signup, then should create user and return auth response")
  void givenValidSignupRequest_whenSignup_thenShouldCreateUserAndReturnAuthResponse() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

    // When
    AuthResponse response = userService.signup(signupRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getUsername()).isEqualTo("testuser");
    assertThat(response.getEmail()).isEqualTo("test@example.com");

    verify(userRepository).existsByUsername("testuser");
    verify(userRepository).existsByEmail("test@example.com");
    verify(passwordEncoder).encode("password123");
    verify(userRepository).save(any(User.class));
    verify(jwtUtil).generateToken("testuser");
  }

  @Test
  @DisplayName("Given username already exists, when signup, then should throw UsernameAlreadyExistsException")
  void givenUsernameAlreadyExists_whenSignup_thenShouldThrowException() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.signup(signupRequest))
        .isInstanceOf(UsernameAlreadyExistsException.class)
        .hasMessageContaining("testuser");

    verify(userRepository).existsByUsername("testuser");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Given email already exists, when signup, then should throw EmailAlreadyExistsException")
  void givenEmailAlreadyExists_whenSignup_thenShouldThrowException() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> userService.signup(signupRequest))
        .isInstanceOf(EmailAlreadyExistsException.class)
        .hasMessageContaining("test@example.com");

    verify(userRepository).existsByUsername("testuser");
    verify(userRepository).existsByEmail("test@example.com");
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  @DisplayName("Given valid login request, when login, then should authenticate and return auth response")
  void givenValidLoginRequest_whenLogin_thenShouldAuthenticateAndReturnAuthResponse() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

    // When
    AuthResponse response = userService.login(loginRequest);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getUsername()).isEqualTo("testuser");
    assertThat(response.getEmail()).isEqualTo("test@example.com");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByUsername("testuser");
    verify(jwtUtil).generateToken("testuser");
  }

  @Test
  @DisplayName("Given user not found, when login, then should throw UserNotFoundException")
  void givenUserNotFound_whenLogin_thenShouldThrowException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> userService.login(loginRequest))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByUsername("testuser");
    verify(jwtUtil, never()).generateToken(anyString());
  }

  @Test
  @DisplayName("Given signup request, when password is encoded, then should use password encoder")
  void givenSignupRequest_whenPasswordEncoded_thenShouldUsePasswordEncoder() {
    // Given
    when(userRepository.existsByUsername(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

    // When
    userService.signup(signupRequest);

    // Then
    verify(passwordEncoder).encode("password123");
  }

  @Test
  @DisplayName("Given login request, when authenticate, then should use authentication manager")
  void givenLoginRequest_whenAuthenticate_thenShouldUseAuthenticationManager() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(anyString())).thenReturn("jwt-token");

    // When
    userService.login(loginRequest);

    // Then
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }
}
