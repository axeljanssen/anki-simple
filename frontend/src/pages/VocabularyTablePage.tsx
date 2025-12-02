import React, { useState, useEffect, MouseEvent, ChangeEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { vocabularyAPI, tagAPI } from '@/services/api'
import VocabularyTable from '@/components/VocabularyTable'
import VocabularyForm from '@/components/VocabularyForm'
import type { VocabularyCard, Tag, VocabularyFormData, SortableField, SortDirection } from '@/types'
import { AxiosError } from 'axios'

const VocabularyTablePage = (): React.JSX.Element => {
  const [searchParams, setSearchParams] = useSearchParams()
  const [cards, setCards] = useState<VocabularyCard[]>([])
  const [tags, setTags] = useState<Tag[]>([])
  const [showForm, setShowForm] = useState<boolean>(false)
  const [editingCard, setEditingCard] = useState<VocabularyCard | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [loadingCard, setLoadingCard] = useState<boolean>(false)
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  // Get current sort and search state from URL
  const sortBy = searchParams.get('sortBy') as SortableField | null
  const sortDirection = searchParams.get('sortDirection') as SortDirection | null
  const searchTerm = searchParams.get('searchTerm') || ''

  useEffect(() => {
    loadCards()
    loadTags()
  }, [searchParams])

  const loadCards = async (): Promise<void> => {
    setLoading(true)
    try {
      const params = {
        sortBy: sortBy || undefined,
        sortDirection: sortDirection || undefined,
        searchTerm: searchTerm || undefined,
      }
      const response = await vocabularyAPI.getAll(params)
      setCards(response.data)
    } catch (error) {
      const axiosError = error as AxiosError
      console.error('Failed to load cards:', axiosError)
    } finally {
      setLoading(false)
    }
  }

  const loadTags = async (): Promise<void> => {
    try {
      const response = await tagAPI.getAll()
      setTags(response.data)
    } catch (error) {
      const axiosError = error as AxiosError
      console.error('Failed to load tags:', axiosError)
    }
  }

  const handleSort = (field: SortableField): void => {
    const newParams = new URLSearchParams(searchParams)

    // If clicking the same field, toggle direction
    if (sortBy === field) {
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc'
      newParams.set('sortDirection', newDirection)
    } else {
      // New field, default to ascending
      newParams.set('sortBy', field)
      newParams.set('sortDirection', 'asc')
    }

    setSearchParams(newParams)
  }

  const handleSearchChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const value = e.target.value
    const newParams = new URLSearchParams(searchParams)

    if (value) {
      newParams.set('searchTerm', value)
    } else {
      newParams.delete('searchTerm')
    }

    setSearchParams(newParams)
  }

  const handleClearSearch = (): void => {
    const newParams = new URLSearchParams(searchParams)
    newParams.delete('searchTerm')
    setSearchParams(newParams)
  }

  const handleSaveCard = async (cardData: VocabularyFormData): Promise<void> => {
    try {
      if (editingCard) {
        await vocabularyAPI.update(editingCard.id, cardData)
      } else {
        await vocabularyAPI.create(cardData)
      }
      setShowForm(false)
      setEditingCard(null)
      loadCards()
    } catch (error) {
      const axiosError = error as AxiosError
      console.error('Failed to save card:', axiosError)
    }
  }

  const handleEditCard = async (card: VocabularyCard): Promise<void> => {
    setLoadingCard(true)
    try {
      // Fetch fresh card data from server
      const response = await vocabularyAPI.getById(card.id)
      setEditingCard(response.data)
      setShowForm(true)
    } catch (error) {
      const axiosError = error as AxiosError<{ detail?: string }>

      if (axiosError.response?.status === 404) {
        alert('This card no longer exists. Refreshing the list...')
        loadCards()
      } else if (axiosError.response?.status === 401 || axiosError.response?.status === 403) {
        alert('You do not have permission to edit this card.')
      } else {
        console.error('Failed to load card:', axiosError)
        alert('Failed to load card for editing. Please try again.')
      }
    } finally {
      setLoadingCard(false)
    }
  }

  const handleDeleteCard = async (id: number): Promise<void> => {
    if (window.confirm('Are you sure you want to delete this card?')) {
      try {
        await vocabularyAPI.delete(id)
        setCards(prevCards => prevCards.filter(card => card.id !== id))
      } catch (error) {
        const axiosError = error as AxiosError
        console.error('Failed to delete card:', axiosError)
        loadCards()
      }
    }
  }

  const handleAddNew = (): void => {
    setEditingCard(null)
    setShowForm(true)
  }

  const handleOverlayClick = (e: MouseEvent<HTMLDivElement>): void => {
    if ((e.target as HTMLElement).classList.contains('form-modal-overlay')) {
      setShowForm(false)
      setEditingCard(null)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-gradient-to-br from-blue-600 to-blue-700 text-white px-10 py-5 flex justify-between items-center shadow-md md:flex-row md:px-10 md:py-5 max-md:flex-col max-md:px-5 max-md:py-4 max-md:gap-4">
        <h1 className="m-0 text-3xl font-semibold max-md:text-2xl">Simple Anki - Table View</h1>
        <div className="flex items-center gap-4 max-md:w-full max-md:justify-between max-md:flex-wrap">
          <button
            onClick={() => navigate('/dashboard')}
            className="bg-white/20 text-white px-5 py-2.5 border-none rounded-md cursor-pointer font-medium transition-all duration-300 hover:bg-white/30 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-black/20"
          >
            Grid View
          </button>
          <button
            onClick={() => navigate('/tags')}
            className="bg-white/20 text-white px-5 py-2.5 border-none rounded-md cursor-pointer font-medium transition-all duration-300 hover:bg-white/30 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-black/20"
          >
            Manage Tags
          </button>
          <span className="text-white text-sm">Welcome, {user?.username}</span>
          <button
            onClick={logout}
            className="bg-white/20 text-white px-5 py-2.5 border-none rounded-md cursor-pointer font-medium transition-all duration-300 hover:bg-white/30 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-black/20"
          >
            Logout
          </button>
        </div>
      </header>

      <div className="max-w-[1200px] mx-auto py-10 px-5 max-md:py-5 max-md:px-4">
        <div className="flex justify-between items-center mb-5 gap-5 max-md:flex-col max-md:items-stretch max-md:gap-4">
          <button
            onClick={handleAddNew}
            className="bg-gradient-to-br from-blue-600 to-blue-700 text-white px-6 py-3 border-none rounded-lg cursor-pointer font-semibold text-base transition-all duration-300 shadow-lg shadow-blue-600/30 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-blue-600/40 max-md:w-full"
          >
            Add New Card
          </button>
          <div className="relative flex-1 max-w-[500px] max-md:max-w-none">
            <input
              type="text"
              className="w-full py-3 pr-10 pl-4 border-2 border-gray-300 rounded-lg text-sm transition-all duration-300 focus:outline-none focus:border-blue-600 focus:shadow-[0_0_0_3px_rgba(37,99,235,0.1)]"
              placeholder="Search cards by front, back, or example..."
              value={searchTerm}
              onChange={handleSearchChange}
            />
            {searchTerm && (
              <button
                onClick={handleClearSearch}
                className="absolute right-2 top-1/2 -translate-y-1/2 bg-transparent border-none text-gray-400 text-xl cursor-pointer px-2 py-1 leading-none transition-colors duration-200 hover:text-gray-800"
                aria-label="Clear search"
              >
                ✕
              </button>
            )}
          </div>
        </div>

        {loading ? (
          <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center text-gray-600 text-base">
            Loading...
          </div>
        ) : (
          <VocabularyTable
            cards={cards}
            onEdit={handleEditCard}
            onDelete={handleDeleteCard}
            sortBy={sortBy || undefined}
            sortDirection={sortDirection || undefined}
            onSort={handleSort}
            loading={loading}
          />
        )}

        {loadingCard && (
          <div className="fixed inset-0 bg-black/30 flex justify-center items-center z-[999]">
            <div className="bg-white py-4 px-6 rounded-lg shadow-lg text-gray-800">
              Loading card...
            </div>
          </div>
        )}

        {showForm && (
          <div className="form-modal-overlay fixed inset-0 bg-black/50 flex justify-center items-center z-[1000]" onClick={handleOverlayClick}>
            <div className="bg-white rounded-lg p-8 max-w-[600px] w-[90%] max-h-[90vh] overflow-y-auto shadow-2xl animate-modal-slide max-md:w-[95%] max-md:p-5" onClick={(e: MouseEvent<HTMLDivElement>) => e.stopPropagation()}>
              <VocabularyForm
                card={editingCard}
                tags={tags}
                onSave={handleSaveCard}
                onCancel={() => {
                  setShowForm(false)
                  setEditingCard(null)
                }}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default VocabularyTablePage
