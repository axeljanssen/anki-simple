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
  sourceLanguage: string | null
  targetLanguage: string | null
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

export interface VocabularyFormData {
  front: string
  back: string
  exampleSentence: string
  sourceLanguage: string
  targetLanguage: string
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
