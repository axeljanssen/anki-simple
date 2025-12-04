import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Review from './Review'
import { vocabularyAPI, reviewAPI } from '@/services/api'
import type { VocabularyCard } from '@/types'
import type { AxiosResponse } from 'axios'

vi.mock('@/services/api', () => ({
  authAPI: {
    login: vi.fn(),
    signup: vi.fn(),
  },
  vocabularyAPI: {
    getAll: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    getDue: vi.fn(),
    getDueCount: vi.fn(),
  },
  reviewAPI: {
    review: vi.fn(),
  },
  tagAPI: {
    getAll: vi.fn(),
    create: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockedVocabularyAPI = vi.mocked(vocabularyAPI)
const mockedReviewAPI = vi.mocked(reviewAPI)

// Helper to create mock AxiosResponse
const createMockAxiosResponse = <T,>(data: T): AxiosResponse<T> => ({
  data,
  status: 200,
  statusText: 'OK',
  headers: {},
  config: {} as AxiosResponse['config'],
})

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  }
})

const mockCard1: VocabularyCard = {
  id: 1,
  front: 'Hello',
  back: 'Hola',
  exampleSentence: 'Hello, how are you?',
  languageSelection: 'EN_ES',
  audioUrl: null,
  tags: [],
  easeFactor: 2.5,
  intervalDays: 1,
  repetitions: 0,
  lastReviewed: null,
  nextReview: new Date().toISOString(),
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
}

const mockCard2: VocabularyCard = {
  id: 2,
  front: 'Goodbye',
  back: 'Adiós',
  exampleSentence: 'Goodbye, see you later!',
  languageSelection: 'EN_ES',
  audioUrl: 'https://example.com/audio.mp3',
  tags: [],
  easeFactor: 2.5,
  intervalDays: 1,
  repetitions: 0,
  lastReviewed: null,
  nextReview: new Date().toISOString(),
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
}

const renderReview = () => {
  return render(
    <BrowserRouter>
      <Review />
    </BrowserRouter>
  )
}

describe('Review Component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockNavigate.mockClear()
  })

  it('should render loading state initially', () => {
    mockedVocabularyAPI.getDue.mockImplementation(() => new Promise(() => {}))

    renderReview()

    expect(screen.getByText('Loading...')).toBeInTheDocument()
  })

  it('should display "no cards due" message when no cards available', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('No cards due for review!')).toBeInTheDocument()
    })

    expect(screen.getByText('Come back later or add more vocabulary cards.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Back to Dashboard' })).toBeInTheDocument()
  })

  it('should navigate to dashboard when clicking back button on empty state', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Back to Dashboard' })).toBeInTheDocument()
    })

    const backButton = screen.getByRole('button', { name: 'Back to Dashboard' })
    fireEvent.click(backButton)

    expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
  })

  it('should display first card when cards are available', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    expect(screen.getByText('English ⇄ Spanish')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    expect(screen.getByText('1 / 1')).toBeInTheDocument()
  })

  it('should show answer when clicking "Show Answer" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    const showAnswerButton = screen.getByRole('button', { name: /Reveal answer/i })
    fireEvent.click(showAnswerButton)

    expect(screen.getByText('Answer:')).toBeInTheDocument()
    expect(screen.getByText('Hola')).toBeInTheDocument()
    expect(screen.getByText('Hello, how are you?')).toBeInTheDocument()
    expect(screen.getByText('How well did you know this?')).toBeInTheDocument()
  })

  it('should show answer when pressing Space key', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: ' ' })

    await waitFor(() => {
      expect(screen.getByText('Answer:')).toBeInTheDocument()
    })

    expect(screen.getByText('Hola')).toBeInTheDocument()
  })

  it('should submit review with quality 0 when clicking "Again" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Again/i })).toBeInTheDocument()
    })

    const againButton = screen.getByRole('button', { name: /Rate as Again/i })
    fireEvent.click(againButton)

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 0,
      })
    })
  })

  it('should submit review with quality 3 when clicking "Hard" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Hard/i })).toBeInTheDocument()
    })

    const hardButton = screen.getByRole('button', { name: /Rate as Hard/i })
    fireEvent.click(hardButton)

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 3,
      })
    })
  })

  it('should submit review with quality 4 when clicking "Good" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Good/i })).toBeInTheDocument()
    })

    const goodButton = screen.getByRole('button', { name: /Rate as Good/i })
    fireEvent.click(goodButton)

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 4,
      })
    })
  })

  it('should submit review with quality 5 when clicking "Easy" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Easy/i })).toBeInTheDocument()
    })

    const easyButton = screen.getByRole('button', { name: /Rate as Easy/i })
    fireEvent.click(easyButton)

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 5,
      })
    })
  })

  it('should advance to next card after reviewing', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Easy/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Rate as Easy/i }))

    // Wait for the 300ms delay and next card to appear
    await waitFor(() => {
      expect(screen.getByText('Goodbye')).toBeInTheDocument()
    }, { timeout: 1000 })

    expect(screen.getByText('2 / 2')).toBeInTheDocument()
  })

  it('should navigate to dashboard after reviewing last card', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Easy/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Rate as Easy/i }))

    // Wait for the 300ms delay and navigation
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
    }, { timeout: 1000 })
  })

  it('should submit review with keyboard shortcuts (1 for quality 0)', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: ' ' })

    await waitFor(() => {
      expect(screen.getByText('Answer:')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '1' })

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 0,
      })
    })
  })

  it('should submit review with keyboard shortcuts (2 for quality 3)', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: ' ' })

    await waitFor(() => {
      expect(screen.getByText('Answer:')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '2' })

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 3,
      })
    })
  })

  it('should submit review with keyboard shortcuts (3 for quality 4)', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: ' ' })

    await waitFor(() => {
      expect(screen.getByText('Answer:')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '3' })

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 4,
      })
    })
  })

  it('should submit review with keyboard shortcuts (4 for quality 5)', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Reveal answer/i })).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: ' ' })

    await waitFor(() => {
      expect(screen.getByText('Answer:')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '4' })

    await waitFor(() => {
      expect(mockedReviewAPI.review).toHaveBeenCalledWith({
        cardId: 1,
        quality: 5,
      })
    })
  })

  it('should navigate to dashboard when pressing Escape key', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: 'Escape' })

    expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
  })

  it('should show keyboard help modal when pressing ?', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '?' })

    await waitFor(() => {
      expect(screen.getByText('Keyboard Shortcuts')).toBeInTheDocument()
    })

    expect(screen.getAllByText('Show Answer').length).toBeGreaterThan(0)
    expect(screen.getByText('Again (Total blackout)')).toBeInTheDocument()
    expect(screen.getByText('Hard (Difficult recall)')).toBeInTheDocument()
    expect(screen.getByText('Good (Some hesitation)')).toBeInTheDocument()
    expect(screen.getByText('Easy (Perfect recall)')).toBeInTheDocument()
  })

  it('should close keyboard help modal when clicking "Got it!" button', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.keyDown(window, { key: '?' })

    await waitFor(() => {
      expect(screen.getByText('Keyboard Shortcuts')).toBeInTheDocument()
    })

    const gotItButton = screen.getByRole('button', { name: 'Got it!' })
    fireEvent.click(gotItButton)

    await waitFor(() => {
      expect(screen.queryByText('Keyboard Shortcuts')).not.toBeInTheDocument()
    })
  })

  it('should show keyboard shortcuts button and open modal when clicked', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    const shortcutsButton = screen.getByRole('button', { name: /Shortcuts/i })
    fireEvent.click(shortcutsButton)

    await waitFor(() => {
      expect(screen.getByText('Keyboard Shortcuts')).toBeInTheDocument()
    })
  })

  it('should show audio player when card has audioUrl', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard2]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Goodbye')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      const audio = document.querySelector('audio')
      expect(audio).toBeInTheDocument()
      expect(audio?.src).toBe('https://example.com/audio.mp3')
    })
  })

  it('should update progress bar as cards are reviewed', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1, mockCard2]))
    mockedReviewAPI.review.mockResolvedValue(createMockAxiosResponse(undefined))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    // Check initial progress (1 of 2 = 50%)
    let progressBar = document.querySelector('[role="progressbar"]')
    expect(progressBar).toHaveAttribute('aria-valuenow', '1')
    expect(progressBar).toHaveAttribute('aria-valuemax', '2')

    // Complete first card
    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Easy/i })).toBeInTheDocument()
    })
    fireEvent.click(screen.getByRole('button', { name: /Rate as Easy/i }))

    // Wait for the 300ms delay and progress update
    await waitFor(() => {
      progressBar = document.querySelector('[role="progressbar"]')
      expect(progressBar).toHaveAttribute('aria-valuenow', '2')
      expect(progressBar).toHaveAttribute('aria-valuemax', '2')
    }, { timeout: 1000 })
  })

  it('should not allow rating before showing answer', async () => {
    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    // Try to press keyboard shortcuts before showing answer
    fireEvent.keyDown(window, { key: '1' })
    fireEvent.keyDown(window, { key: '2' })
    fireEvent.keyDown(window, { key: '3' })
    fireEvent.keyDown(window, { key: '4' })

    // Should not have called review
    expect(mockedReviewAPI.review).not.toHaveBeenCalled()
  })

  it('should handle API errors gracefully when loading cards', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    mockedVocabularyAPI.getDue.mockRejectedValue(new Error('Failed to load'))

    renderReview()

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalled()
    })

    consoleError.mockRestore()
  })

  it('should handle API errors gracefully when submitting review', async () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {})

    mockedVocabularyAPI.getDue.mockResolvedValue(createMockAxiosResponse<VocabularyCard[]>([mockCard1]))
    mockedReviewAPI.review.mockRejectedValue(new Error('Failed to submit'))

    renderReview()

    await waitFor(() => {
      expect(screen.getByText('Hello')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Reveal answer/i }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Rate as Easy/i })).toBeInTheDocument()
    })

    fireEvent.click(screen.getByRole('button', { name: /Rate as Easy/i }))

    await waitFor(() => {
      expect(consoleError).toHaveBeenCalled()
    })

    consoleError.mockRestore()
  })
})
