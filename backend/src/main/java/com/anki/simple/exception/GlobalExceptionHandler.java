package com.anki.simple.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleUserNotFoundException(
      UserNotFoundException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("User Not Found")
        .status(HttpStatus.NOT_FOUND.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(CardNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleCardNotFoundException(
      CardNotFoundException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Card Not Found")
        .status(HttpStatus.NOT_FOUND.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(TagNotFoundException.class)
  public ResponseEntity<ProblemDetail> handleTagNotFoundException(
      TagNotFoundException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Tag Not Found")
        .status(HttpStatus.NOT_FOUND.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
  }

  @ExceptionHandler(UsernameAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleUsernameAlreadyExistsException(
      UsernameAlreadyExistsException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Username Already Exists")
        .status(HttpStatus.CONFLICT.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleEmailAlreadyExistsException(
      EmailAlreadyExistsException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Email Already Exists")
        .status(HttpStatus.CONFLICT.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(TagAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleTagAlreadyExistsException(
      TagAlreadyExistsException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Tag Already Exists")
        .status(HttpStatus.CONFLICT.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ProblemDetail> handleUnauthorizedException(
      UnauthorizedException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Unauthorized")
        .status(HttpStatus.UNAUTHORIZED.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ProblemDetail> handleIllegalArgumentException(
      IllegalArgumentException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Bad Request")
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(ex.getMessage())
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleBadCredentialsException(
      org.springframework.security.authentication.BadCredentialsException ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Unauthorized")
        .status(HttpStatus.UNAUTHORIZED.value())
        .detail("Invalid username or password")
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleMethodArgumentNotValidException(
      org.springframework.web.bind.MethodArgumentNotValidException ex, WebRequest request) {
    String errors = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .reduce((a, b) -> a + ", " + b)
        .orElse("Validation failed");

    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Bad Request")
        .status(HttpStatus.BAD_REQUEST.value())
        .detail(errors)
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleGenericException(
      Exception ex, WebRequest request) {
    ProblemDetail problem = ProblemDetail.builder()
        .type("about:blank")
        .title("Internal Server Error")
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .detail("An unexpected error occurred")
        .instance(request.getDescription(false).replace("uri=", ""))
        .timestamp(LocalDateTime.now())
        .build();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
  }
}
