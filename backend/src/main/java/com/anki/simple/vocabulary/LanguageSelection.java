package com.anki.simple.vocabulary;

public enum LanguageSelection {
  DE_FR("German ⇄ French"),
  DE_ES("German ⇄ Spanish"),
  EN_ES("English ⇄ Spanish"),
  EN_FR("English ⇄ French"),
  EN_DE("English ⇄ German"),
  FR_ES("French ⇄ Spanish"),
  EN_IT("English ⇄ Italian"),
  DE_IT("German ⇄ Italian"),
  FR_IT("French ⇄ Italian"),
  ES_IT("Spanish ⇄ Italian");

  private final String displayName;

  LanguageSelection(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
