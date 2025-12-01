// Core data models

export interface User {
  username: string
  email: string
  token: string
}

export interface VocabularyCard {
  id: number
  front: string
  back: string
  exampleSentence: string | null
  languageSelection: LanguageSelection | null
  audioUrl: string | null
  tags: Tag[]
  easeFactor: number
  intervalDays: number
  repetitions: number
  nextReview: string | null
  createdAt: string
  updatedAt: string
}

export interface Tag {
  id: number
  name: string
  color: string
}

export enum LanguageSelection {
  DE_FR = 'DE_FR',
  DE_ES = 'DE_ES'
}

export const LANGUAGE_SELECTION_LABELS: Record<LanguageSelection, string> = {
  [LanguageSelection.DE_FR]: 'German ⇄ French',
  [LanguageSelection.DE_ES]: 'German ⇄ Spanish'
}

export interface VocabularyFormData {
  front: string
  back: string
  exampleSentence: string
  languageSelection: LanguageSelection
  audioUrl: string
  tagIds: number[]
}

export interface LoginCredentials {
  username: string
  password: string
}

export interface SignupData {
  username: string
  email: string
  password: string
}

export interface ReviewSubmission {
  cardId: number
  quality: 0 | 1 | 2 | 3 | 4 | 5
}
