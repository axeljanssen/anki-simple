import React, { createContext, useState, useContext, useEffect, useCallback, useMemo, ReactNode } from 'react'
import { AxiosError } from 'axios'
import { authAPI } from '@/services/api'
import { User, LoginCredentials, SignupData, AuthContextValue, AuthResult } from '@/types'

export const AuthContext = createContext<AuthContextValue | null>(null)

interface AuthProviderProps {
  children: ReactNode
}

export const AuthProvider = ({ children }: AuthProviderProps): React.JSX.Element => {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState<boolean>(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    const email = localStorage.getItem('email')

    if (token && username && email) {
      setUser({ username, email, token })
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (credentials: LoginCredentials): Promise<AuthResult> => {
    try {
      const response = await authAPI.login(credentials)
      const { token, username, email } = response.data

      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('email', email)

      setUser({ username, email, token })
      return { success: true }
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>
      return { success: false, error: axiosError.response?.data?.message || 'Login failed' }
    }
  }, [])

  const signup = useCallback(async (userData: SignupData): Promise<AuthResult> => {
    try {
      const response = await authAPI.signup(userData)
      const { token, username, email } = response.data

      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('email', email)

      setUser({ username, email, token })
      return { success: true }
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>
      return { success: false, error: axiosError.response?.data?.message || 'Signup failed' }
    }
  }, [])

  const logout = useCallback((): void => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('email')
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, login, signup, logout, loading }),
    [user, login, signup, logout, loading]
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = (): AuthContextValue => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
