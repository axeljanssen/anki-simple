import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import { AuthContext } from '@/context/AuthContext'
import type { AuthContextValue } from '@/types'

const renderProtectedRoute = (authContextValue: AuthContextValue, initialPath = '/') => {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AuthContext.Provider value={authContextValue}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </AuthContext.Provider>
    </MemoryRouter>
  )
}

describe('ProtectedRoute Component', () => {
  it('should show loading state when loading is true', () => {
    const authValue: AuthContextValue = {
      user: null,
      loading: true,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
    }

    renderProtectedRoute(authValue)

    expect(screen.getByText('Loading...')).toBeInTheDocument()
  })

  it('should render children when user is authenticated', () => {
    const authValue: AuthContextValue = {
      user: { username: 'testuser', email: 'test@example.com', token: 'mock-token' },
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
    }

    renderProtectedRoute(authValue)

    expect(screen.getByText('Protected Content')).toBeInTheDocument()
  })

  it('should redirect to login when user is not authenticated', () => {
    const authValue: AuthContextValue = {
      user: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
    }

    renderProtectedRoute(authValue, '/')

    expect(screen.getByText('Login Page')).toBeInTheDocument()
    expect(screen.queryByText('Protected Content')).not.toBeInTheDocument()
  })
})
