package com.anki.simple.vocabulary;

public enum LanguageSelection {
  DE_FR("German ⇄ French"),
  DE_ES("German ⇄ Spanish");

  private final String displayName;

  LanguageSelection(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
