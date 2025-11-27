import { describe, it, expect, beforeEach, vi } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { AuthProvider, useAuth } from './AuthContext'
import { authAPI } from '../services/api'

vi.mock('../services/api')

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
      const mockCredentials = { username: 'testuser', password: 'password123' }
      const mockResponse = {
        data: {
          token: 'mock-token',
          username: 'testuser',
          email: 'test@example.com',
        },
      }

      authAPI.login.mockResolvedValue(mockResponse)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let loginResult
      await act(async () => {
        loginResult = await result.current.login(mockCredentials)
      })

      expect(authAPI.login).toHaveBeenCalledWith(mockCredentials)
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
      const mockCredentials = { username: 'testuser', password: 'wrongpassword' }
      const mockError = {
        response: {
          data: {
            message: 'Invalid credentials',
          },
        },
      }

      authAPI.login.mockRejectedValue(mockError)

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
      const mockCredentials = { username: 'testuser', password: 'wrongpassword' }
      authAPI.login.mockRejectedValue(new Error('Network error'))

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
      const mockUserData = {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
      }
      const mockResponse = {
        data: {
          token: 'mock-token',
          username: 'newuser',
          email: 'newuser@example.com',
        },
      }

      authAPI.signup.mockResolvedValue(mockResponse)

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      })

      let signupResult
      await act(async () => {
        signupResult = await result.current.signup(mockUserData)
      })

      expect(authAPI.signup).toHaveBeenCalledWith(mockUserData)
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
      const mockUserData = {
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

      authAPI.signup.mockRejectedValue(mockError)

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
      const mockUserData = {
        username: 'newuser',
        email: 'newuser@example.com',
        password: 'password123',
      }
      authAPI.signup.mockRejectedValue(new Error('Network error'))

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
