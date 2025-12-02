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

export interface TagFormData {
  name: string
  color: string
}

export enum LanguageSelection {
  DE_FR = 'DE_FR',
  DE_ES = 'DE_ES',
  EN_ES = 'EN_ES',
  EN_FR = 'EN_FR',
  EN_DE = 'EN_DE',
  FR_ES = 'FR_ES',
  EN_IT = 'EN_IT',
  DE_IT = 'DE_IT',
  FR_IT = 'FR_IT',
  ES_IT = 'ES_IT'
}

export const LANGUAGE_SELECTION_LABELS: Record<LanguageSelection, string> = {
  [LanguageSelection.DE_FR]: 'German ⇄ French',
  [LanguageSelection.DE_ES]: 'German ⇄ Spanish',
  [LanguageSelection.EN_ES]: 'English ⇄ Spanish',
  [LanguageSelection.EN_FR]: 'English ⇄ French',
  [LanguageSelection.EN_DE]: 'English ⇄ German',
  [LanguageSelection.FR_ES]: 'French ⇄ Spanish',
  [LanguageSelection.EN_IT]: 'English ⇄ Italian',
  [LanguageSelection.DE_IT]: 'German ⇄ Italian',
  [LanguageSelection.FR_IT]: 'French ⇄ Italian',
  [LanguageSelection.ES_IT]: 'Spanish ⇄ Italian'
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
