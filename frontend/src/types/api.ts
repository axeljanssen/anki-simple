// API request and response types

export interface AuthResponse {
  token: string
  username: string
  email: string
}

export interface ProblemDetails {
  type: string
  title: string
  status: number
  detail: string
  instance: string
  timestamp: string
}

// Sorting types
export type SortDirection = 'asc' | 'desc'
export type SortableField = 'front' | 'back'

export interface VocabularyQueryParams {
  sortBy?: SortableField
  sortDirection?: SortDirection
  searchTerm?: string
}
