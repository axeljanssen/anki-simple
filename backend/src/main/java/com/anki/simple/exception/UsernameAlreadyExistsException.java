package com.anki.simple.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
  public UsernameAlreadyExistsException(String username) {
    super("Username already exists: " + username);
  }
}
