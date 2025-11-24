package com.anki.simple.exception;

public class TagAlreadyExistsException extends RuntimeException {
  public TagAlreadyExistsException(String tagName) {
    super("Tag already exists: " + tagName);
  }
}
