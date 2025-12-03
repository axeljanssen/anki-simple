import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import VocabularyList from './VocabularyList'
import type { VocabularyCard } from '@/types'
import { LanguageSelection } from '@/types'

// Helper to create complete mock cards with all required properties
const createMockCard = (overrides: Partial<VocabularyCard> = {}): VocabularyCard => ({
  id: 1,
  front: 'Test',
  back: 'Prueba',
  exampleSentence: null,
  languageSelection: LanguageSelection.EN_ES,
  audioUrl: null,
  tags: [],
  easeFactor: 2.5,
  intervalDays: 1,
  repetitions: 0,
  nextReview: null,
  createdAt: '2024-01-01T00:00:00',
  updatedAt: '2024-01-01T00:00:00',
  ...overrides,
})

describe('VocabularyList Component', () => {
  it('should display empty state when no cards are provided', () => {
    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={[]} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(
      screen.getByText(/No vocabulary cards yet. Create your first card to get started!/)
    ).toBeInTheDocument()
  })

  it('should render list of vocabulary cards', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        exampleSentence: 'Hello, how are you?',
        languageSelection: LanguageSelection.DE_ES,
        tags: [{ id: 1, name: 'Spanish', color: '#ff0000' }],
      }),
      createMockCard({
        id: 2,
        front: 'Goodbye',
        back: 'Adiós',
        exampleSentence: null,
        languageSelection: LanguageSelection.DE_ES,
        easeFactor: 2.3,
        intervalDays: 3,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.getByText('Your Vocabulary Cards')).toBeInTheDocument()
    expect(screen.getByText('Hello')).toBeInTheDocument()
    expect(screen.getByText('Hola')).toBeInTheDocument()
    expect(screen.getByText('Goodbye')).toBeInTheDocument()
    expect(screen.getByText('Adiós')).toBeInTheDocument()
  })

  it('should display example sentence when provided', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        exampleSentence: 'Hello, how are you?',
        languageSelection: LanguageSelection.DE_ES,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.getByText(/Hello, how are you\?/)).toBeInTheDocument()
  })

  it('should not display example sentence when not provided', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.queryByText(/Example:/)).not.toBeInTheDocument()
  })

  it('should display language badge when languages are provided', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.getByText('German ⇄ Spanish')).toBeInTheDocument()
  })

  it('should display tags when provided', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
        tags: [
          { id: 1, name: 'Spanish', color: '#ff0000' },
          { id: 2, name: 'Greetings', color: '#00ff00' },
        ],
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.getByText('Spanish')).toBeInTheDocument()
    expect(screen.getByText('Greetings')).toBeInTheDocument()
  })

  it('should display card statistics', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
        easeFactor: 2.5,
        intervalDays: 3,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    expect(screen.getByText(/Ease: 2.50 \| Interval: 3 days/)).toBeInTheDocument()
  })

  it('should call onEdit with card when edit button is clicked', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    const editButton = screen.getByText('Edit')
    fireEvent.click(editButton)

    expect(mockOnEdit).toHaveBeenCalledWith(mockCards[0])
  })

  it('should call onDelete with card id when delete button is clicked', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    const deleteButton = screen.getByText('Delete')
    fireEvent.click(deleteButton)

    expect(mockOnDelete).toHaveBeenCalledWith(1)
  })

  it('should render multiple cards with correct actions', () => {
    const mockCards: VocabularyCard[] = [
      createMockCard({
        id: 1,
        front: 'Hello',
        back: 'Hola',
        languageSelection: LanguageSelection.DE_ES,
      }),
      createMockCard({
        id: 2,
        front: 'Goodbye',
        back: 'Adiós',
        languageSelection: LanguageSelection.DE_ES,
        easeFactor: 2.3,
        intervalDays: 3,
      }),
    ]

    const mockOnEdit = vi.fn()
    const mockOnDelete = vi.fn()

    render(<VocabularyList cards={mockCards} onEdit={mockOnEdit} onDelete={mockOnDelete} />)

    const editButtons = screen.getAllByText('Edit')
    const deleteButtons = screen.getAllByText('Delete')

    expect(editButtons).toHaveLength(2)
    expect(deleteButtons).toHaveLength(2)

    fireEvent.click(editButtons[1]!)
    expect(mockOnEdit).toHaveBeenCalledWith(mockCards[1])

    fireEvent.click(deleteButtons[0]!)
    expect(mockOnDelete).toHaveBeenCalledWith(1)
  })
})
