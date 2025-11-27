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
