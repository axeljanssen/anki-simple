package com.anki.simple.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void handleUserNotFoundException_shouldReturn404ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/user-not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("User Not Found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("User not found with id: 999"))
        .andExpect(jsonPath("$.instance").value("/test/user-not-found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleCardNotFoundException_shouldReturn404ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/card-not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Card Not Found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Vocabulary card not found with id: 999"))
        .andExpect(jsonPath("$.instance").value("/test/card-not-found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleTagNotFoundException_shouldReturn404ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/tag-not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Tag Not Found"))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Tag not found with id: 999"))
        .andExpect(jsonPath("$.instance").value("/test/tag-not-found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleUsernameAlreadyExists_shouldReturn409ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/username-exists"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Username Already Exists"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("Username already exists: testuser"))
        .andExpect(jsonPath("$.instance").value("/test/username-exists"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleEmailAlreadyExists_shouldReturn409ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/email-exists"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Email Already Exists"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("Email already exists: test@example.com"))
        .andExpect(jsonPath("$.instance").value("/test/email-exists"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleTagAlreadyExists_shouldReturn409ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/tag-exists"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Tag Already Exists"))
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("Tag already exists: grammar"))
        .andExpect(jsonPath("$.instance").value("/test/tag-exists"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleUnauthorized_shouldReturn401ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/unauthorized"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.detail").value("Access denied"))
        .andExpect(jsonPath("$.instance").value("/test/unauthorized"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleIllegalArgument_shouldReturn400ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/illegal-argument"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("Invalid argument provided"))
        .andExpect(jsonPath("$.instance").value("/test/illegal-argument"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleBadCredentials_shouldReturn401ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/bad-credentials"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Unauthorized"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.detail").value("Invalid username or password"))
        .andExpect(jsonPath("$.instance").value("/test/bad-credentials"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleMethodArgumentNotValid_shouldReturn400WithValidationErrors() throws Exception {
    mockMvc.perform(get("/test/validation-error"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.detail").value("username: must not be blank, email: must be a valid email"))
        .andExpect(jsonPath("$.instance").value("/test/validation-error"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void handleGenericException_shouldReturn500ProblemDetail() throws Exception {
    mockMvc.perform(get("/test/generic-error"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.type").value("about:blank"))
        .andExpect(jsonPath("$.title").value("Internal Server Error"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.detail").value("Something went wrong"))
        .andExpect(jsonPath("$.instance").value("/test/generic-error"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  /**
   * Test controller that throws various exceptions to trigger exception handlers
   */
  @RestController
  @RequestMapping("/test")
  static class TestController {

    @GetMapping("/user-not-found")
    public void throwUserNotFound() {
      throw new UserNotFoundException(999L);
    }

    @GetMapping("/card-not-found")
    public void throwCardNotFound() {
      throw new CardNotFoundException(999L);
    }

    @GetMapping("/tag-not-found")
    public void throwTagNotFound() {
      throw new TagNotFoundException(999L);
    }

    @GetMapping("/username-exists")
    public void throwUsernameExists() {
      throw new UsernameAlreadyExistsException("testuser");
    }

    @GetMapping("/email-exists")
    public void throwEmailExists() {
      throw new EmailAlreadyExistsException("test@example.com");
    }

    @GetMapping("/tag-exists")
    public void throwTagExists() {
      throw new TagAlreadyExistsException("grammar");
    }

    @GetMapping("/unauthorized")
    public void throwUnauthorized() {
      throw new UnauthorizedException("Access denied");
    }

    @GetMapping("/illegal-argument")
    public void throwIllegalArgument() {
      throw new IllegalArgumentException("Invalid argument provided");
    }

    @GetMapping("/bad-credentials")
    public void throwBadCredentials() {
      throw new BadCredentialsException("Bad credentials");
    }

    @GetMapping("/validation-error")
    public void throwValidationError() throws MethodArgumentNotValidException {
      // Create MethodParameter from a real method to avoid NPE in exception handling
      try {
        java.lang.reflect.Method method = TestController.class.getMethod("throwValidationError");
        MethodParameter parameter = new MethodParameter(method, -1);

        BindingResult bindingResult = new org.springframework.validation.BeanPropertyBindingResult(
            new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "username", "must not be blank"));
        bindingResult.addError(new FieldError("testObject", "email", "must be a valid email"));

        throw new MethodArgumentNotValidException(parameter, bindingResult);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    @GetMapping("/generic-error")
    public void throwGenericError() {
      throw new RuntimeException("Something went wrong");
    }
  }
}
