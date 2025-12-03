import { describe, it, expect, beforeEach, vi } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { AuthProvider, useAuth } from './AuthContext'
import { authAPI } from '@/services/api'
import type { LoginCredentials, SignupData, AuthResponse } from '@/types'
import type { AxiosResponse } from 'axios'

vi.mock('@/services/api', () => ({
  authAPI: {
    login: vi.fn(),
    signup: vi.fn(),
  },
}))

const mockedAuthAPI = vi.mocked(authAPI)

// Helper to create mock AxiosResponse
const createMockAxiosResponse = <T,>(data: T): AxiosResponse<T> => ({
  data,
  status: 200,
  statusText: 'OK',
  headers: {},
  config: {} as AxiosResponse['config'],
})

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('AuthProvider initialization', () => {
    it('should initialize with no user when localStorage is empty', async () => {
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(result.current.user).toBeNull()
    })

    it('should initialize with user data from localStorage', async () => {
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('username', 'testuser')
      localStorage.setItem('email', 'test@example.com')

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(result.current.user).toEqual({
        username: 'testuser',
        email: 'test@example.com',
        token: 'mock-token',
      })
    })
  })

  describe('login', () => {
    it('should successfully login and set user data', async () => {
      const mockCredentials: LoginCredentials = { username: 'testuser', password: 'password123' }
      const mockResponse = createMockAxiosResponse<AuthResponse>({
        token: 'mock-token',
        username: 'testuser',
        email: 'test@example.com',
      })

      mockedAuthAPI.login.mockResolvedValue(mockResponse)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let loginResult
      await act(async () => {
        loginResult = await result.current.login(mockCredentials)
      })

      expect(mockedAuthAPI.login).toHaveBeenCalledWith(mockCredentials)
      expect(loginResult).toEqual({ success: true })
      expect(result.current.user).toEqual({
        username: 'testuser',
        email: 'test@example.com',
        token: 'mock-token',
      })
      expect(localStorage.getItem('token')).toBe('mock-token')
      expect(localStorage.getItem('username')).toBe('testuser')
      expect(localStorage.getItem('email')).toBe('test@example.com')
    })

    it('should handle login failure', async () => {
      const mockCredentials: LoginCredentials = { username: 'testuser', password: 'wrongpassword' }
      const mockError = {
        response: {
          data: {
            message: 'Invalid credentials',
          },
        },
      }

      mockedAuthAPI.login.mockRejectedValue(mockError)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let loginResult
      await act(async () => {
        loginResult = await result.current.login(mockCredentials)
      })

      expect(loginResult).toEqual({ success: false, error: 'Invalid credentials' })
      expect(result.current.user).toBeNull()
    })

    it('should handle login failure with default error message', async () => {
      const mockCredentials: LoginCredentials = { username: 'testuser', password: 'wrongpassword' }
      mockedAuthAPI.login.mockRejectedValue(new Error('Network error'))

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let loginResult
      await act(async () => {
        loginResult = await result.current.login(mockCredentials)
      })

      expect(loginResult).toEqual({ success: false, error: 'Login failed' })
    })
  })

  describe('signup', () => {
    it('should successfully signup and set user data', async () => {
      const mockUserData: SignupData = {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
      }
      const mockResponse = createMockAxiosResponse<AuthResponse>({
        token: 'mock-token',
        username: 'newuser',
        email: 'newuser@example.com',
      })

      mockedAuthAPI.signup.mockResolvedValue(mockResponse)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let signupResult
      await act(async () => {
        signupResult = await result.current.signup(mockUserData)
      })

      expect(mockedAuthAPI.signup).toHaveBeenCalledWith(mockUserData)
      expect(signupResult).toEqual({ success: true })
      expect(result.current.user).toEqual({
        username: 'newuser',
        email: 'newuser@example.com',
        token: 'mock-token',
      })
      expect(localStorage.getItem('token')).toBe('mock-token')
      expect(localStorage.getItem('username')).toBe('newuser')
      expect(localStorage.getItem('email')).toBe('newuser@example.com')
    })

    it('should handle signup failure', async () => {
      const mockUserData: SignupData = {
        username: 'existinguser',
        email: 'existing@example.com',
        password: 'password123',
      }
      const mockError = {
        response: {
          data: {
            message: 'Username already exists',
          },
        },
      }

      mockedAuthAPI.signup.mockRejectedValue(mockError)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let signupResult
      await act(async () => {
        signupResult = await result.current.signup(mockUserData)
      })

      expect(signupResult).toEqual({ success: false, error: 'Username already exists' })
      expect(result.current.user).toBeNull()
    })

    it('should handle signup failure with default error message', async () => {
      const mockUserData: SignupData = {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
      }
      mockedAuthAPI.signup.mockRejectedValue(new Error('Network error'))

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let signupResult
      await act(async () => {
        signupResult = await result.current.signup(mockUserData)
      })

      expect(signupResult).toEqual({ success: false, error: 'Signup failed' })
    })
  })

  describe('logout', () => {
    it('should clear user data and localStorage', async () => {
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('username', 'testuser')
      localStorage.setItem('email', 'test@example.com')

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      await waitFor(() => {
        expect(result.current.loading).toBe(false)
      })

      expect(result.current.user).not.toBeNull()

      act(() => {
        result.current.logout()
      })

      expect(result.current.user).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('username')).toBeNull()
      expect(localStorage.getItem('email')).toBeNull()
    })
  })

  describe('useAuth hook', () => {
    it('should throw error when used outside AuthProvider', () => {
      expect(() => {
        renderHook(() => useAuth())
      }).toThrow('useAuth must be used within an AuthProvider')
    })
  })
})
