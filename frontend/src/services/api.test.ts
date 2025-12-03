import { describe, it, expect, beforeEach, vi, Mock } from 'vitest'
import axios from 'axios'
import { AuthResponse, VocabularyCard, Tag, LanguageSelection } from '@/types'

vi.mock('axios', () => {
  return {
    default: {
      create: vi.fn(() => ({
        interceptors: {
          request: { use: vi.fn(), eject: vi.fn() },
          response: { use: vi.fn(), eject: vi.fn() }
        },
        get: vi.fn(),
        post: vi.fn(),
        put: vi.fn(),
        delete: vi.fn(),
      }))
    }
  }
})

describe('API Service', () => {
  let mockAxiosInstance: {
    interceptors: {
      request: { use: Mock; eject: Mock }
      response: { use: Mock; eject: Mock }
    }
    get: Mock
    post: Mock
    put: Mock
    delete: Mock
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    localStorage.clear()

    // Reset modules to get fresh instance
    vi.resetModules()

    // Create a fresh mock axios instance
    mockAxiosInstance = {
      interceptors: {
        request: { use: vi.fn(), eject: vi.fn() },
        response: { use: vi.fn(), eject: vi.fn() }
      },
      get: vi.fn(),
      post: vi.fn(),
      put: vi.fn(),
      delete: vi.fn(),
    }

    ;(axios.create as Mock).mockReturnValue(mockAxiosInstance)
  })

  describe('authAPI', () => {
    it('should call signup endpoint with correct data', async () => {
      const mockData = { username: 'testuser', email: 'test@example.com', password: 'password123' }
      const mockResponse: { data: AuthResponse } = {
        data: { token: 'mock-token', username: 'testuser', email: 'test@example.com' }
      }

      mockAxiosInstance.post.mockResolvedValue(mockResponse)

      // Import after setting up mocks
      const { authAPI } = await import('./api.ts')
      await authAPI.signup(mockData)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/auth/signup', mockData)
    })

    it('should call login endpoint with correct credentials', async () => {
      const mockCredentials = { username: 'testuser', password: 'password123' }
      const mockResponse: { data: AuthResponse } = {
        data: { token: 'mock-token', username: 'testuser', email: 'test@example.com' }
      }

      mockAxiosInstance.post.mockResolvedValue(mockResponse)

      const { authAPI } = await import('./api.ts')
      await authAPI.login(mockCredentials)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/auth/login', mockCredentials)
    })
  })

  describe('vocabularyAPI', () => {
    it('should get all vocabulary cards', async () => {
      const mockCards: Partial<VocabularyCard>[] = [{ id: 1, front: 'Hello', back: 'Hola' }]
      mockAxiosInstance.get.mockResolvedValue({ data: mockCards })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.getAll()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/vocabulary')
    })

    it('should get due vocabulary cards', async () => {
      const mockDueCards: Partial<VocabularyCard>[] = [{ id: 1, front: 'Hello', back: 'Hola' }]
      mockAxiosInstance.get.mockResolvedValue({ data: mockDueCards })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.getDue()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/vocabulary/due')
    })

    it('should get due count', async () => {
      mockAxiosInstance.get.mockResolvedValue({ data: { count: 5 } })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.getDueCount()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/vocabulary/due/count')
    })

    it('should get total count', async () => {
      mockAxiosInstance.get.mockResolvedValue({ data: 10 })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.getTotalCount()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/vocabulary/count')
    })

    it('should create a new vocabulary card', async () => {
      const mockCard = { front: 'Hello', back: 'Hola', exampleSentence: '', languageSelection: LanguageSelection.DE_FR, audioUrl: '', tagIds: [] }
      mockAxiosInstance.post.mockResolvedValue({ data: { id: 1, ...mockCard } })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.create(mockCard)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/vocabulary', mockCard)
    })

    it('should update a vocabulary card', async () => {
      const cardId = 1
      const updatedCard = { front: 'Hello', back: 'Hola', exampleSentence: '', languageSelection: LanguageSelection.DE_FR, audioUrl: '', tagIds: [] }
      mockAxiosInstance.put.mockResolvedValue({ data: { id: cardId, ...updatedCard } })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.update(cardId, updatedCard)

      expect(mockAxiosInstance.put).toHaveBeenCalledWith(`/vocabulary/${cardId}`, updatedCard)
    })

    it('should delete a vocabulary card', async () => {
      const cardId = 1
      mockAxiosInstance.delete.mockResolvedValue({ data: { success: true } })

      const { vocabularyAPI } = await import('./api.ts')
      await vocabularyAPI.delete(cardId)

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith(`/vocabulary/${cardId}`)
    })
  })

  describe('reviewAPI', () => {
    it('should submit a review', async () => {
      const reviewData = { cardId: 1, quality: 4 as 0 | 1 | 2 | 3 | 4 | 5 }
      mockAxiosInstance.post.mockResolvedValue({ data: { success: true } })

      const { reviewAPI } = await import('./api.ts')
      await reviewAPI.review(reviewData)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/review', reviewData)
    })
  })

  describe('tagAPI', () => {
    it('should get all tags', async () => {
      const mockTags: Tag[] = [{ id: 1, name: 'Spanish', color: '#ff0000' }]
      mockAxiosInstance.get.mockResolvedValue({ data: mockTags })

      const { tagAPI } = await import('./api.ts')
      await tagAPI.getAll()

      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/tags')
    })

    it('should create a new tag', async () => {
      const mockTag = { name: 'French', color: '#00ff00' }
      mockAxiosInstance.post.mockResolvedValue({ data: { id: 2, ...mockTag } })

      const { tagAPI } = await import('./api.ts')
      await tagAPI.create(mockTag)

      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/tags', mockTag)
    })

    it('should delete a tag', async () => {
      const tagId = 1
      mockAxiosInstance.delete.mockResolvedValue({ data: { success: true } })

      const { tagAPI } = await import('./api.ts')
      await tagAPI.delete(tagId)

      expect(mockAxiosInstance.delete).toHaveBeenCalledWith(`/tags/${tagId}`)
    })
  })
})
