// Context types
import { User, LoginCredentials, SignupData } from './models'

export interface AuthResult {
  success: boolean
  error?: string
}

export interface AuthContextValue {
  user: User | null
  loading: boolean
  login: (credentials: LoginCredentials) => Promise<AuthResult>
  signup: (userData: SignupData) => Promise<AuthResult>
  logout: () => void
}
