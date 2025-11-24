package com.anki.simple.exception;

public class CardNotFoundException extends RuntimeException {
  public CardNotFoundException(Long cardId) {
    super("Vocabulary card not found with id: " + cardId);
  }
}
