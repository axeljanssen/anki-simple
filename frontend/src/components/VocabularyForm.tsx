import React, { useState, useEffect, FormEvent, ChangeEvent } from 'react'
import type { VocabularyFormProps, VocabularyFormData } from '@/types'
import { LanguageSelection, LANGUAGE_SELECTION_LABELS } from '@/types'

const VocabularyForm = ({ card, tags, onSave, onCancel }: VocabularyFormProps): React.JSX.Element => {
  const [formData, setFormData] = useState<VocabularyFormData>(() => {
    if (card) {
      return {
        front: card.front || '',
        back: card.back || '',
        exampleSentence: card.exampleSentence || '',
        languageSelection: card.languageSelection || LanguageSelection.DE_FR,
        audioUrl: card.audioUrl || '',
        tagIds: card.tags ? card.tags.map((t) => t.id) : [],
      }
    }
    return {
      front: '',
      back: '',
      exampleSentence: '',
      languageSelection: LanguageSelection.DE_FR,
      audioUrl: '',
      tagIds: [],
    }
  })
  const [prevCardId, setPrevCardId] = useState<number | undefined>(card?.id)

  // Reset form when switching between different cards
  useEffect(() => {
    if (card?.id !== prevCardId) {
      setPrevCardId(card?.id)
      setFormData(
        card
          ? {
              front: card.front || '',
              back: card.back || '',
              exampleSentence: card.exampleSentence || '',
              languageSelection: card.languageSelection || LanguageSelection.DE_FR,
              audioUrl: card.audioUrl || '',
              tagIds: card.tags ? card.tags.map((t) => t.id) : [],
            }
          : {
              front: '',
              back: '',
              exampleSentence: '',
              languageSelection: LanguageSelection.DE_FR,
              audioUrl: '',
              tagIds: [],
            }
      )
    }
  }, [card, prevCardId])

  const handleSubmit = (e: FormEvent<HTMLFormElement>): void => {
    e.preventDefault()
    onSave(formData)
  }

  const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
    const { name, value } = e.target
    setFormData({ ...formData, [name]: value })
  }

  const handleSelectChange = (e: ChangeEvent<HTMLSelectElement>): void => {
    const { name, value } = e.target
    setFormData({ ...formData, [name]: value as LanguageSelection })
  }

  const handleTagToggle = (tagId: number): void => {
    setFormData((prev) => ({
      ...prev,
      tagIds: prev.tagIds.includes(tagId)
        ? prev.tagIds.filter((id) => id !== tagId)
        : [...prev.tagIds, tagId],
    }))
  }

  return (
    <div className="bg-white p-8 rounded-xl shadow-md mb-8">
      <h3 className="mt-0 mb-5 text-gray-800 text-xl font-semibold">{card ? 'Edit Card' : 'Create New Card'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div className="mb-5">
            <label htmlFor="front" className="block mb-2 text-gray-600 font-medium text-sm">Front (Word/Question) *</label>
            <input
              type="text"
              id="front"
              name="front"
              value={formData.front}
              onChange={handleChange}
              required
              className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            />
          </div>
          <div className="mb-5">
            <label htmlFor="back" className="block mb-2 text-gray-600 font-medium text-sm">Back (Translation/Answer) *</label>
            <input
              type="text"
              id="back"
              name="back"
              value={formData.back}
              onChange={handleChange}
              required
              className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
            />
          </div>
        </div>

        <div className="mb-5">
          <label htmlFor="exampleSentence" className="block mb-2 text-gray-600 font-medium text-sm">Example Sentence</label>
          <textarea
            id="exampleSentence"
            name="exampleSentence"
            value={formData.exampleSentence}
            onChange={handleChange}
            rows={3}
            className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200 font-sans"
          />
        </div>

        <div className="mb-5">
          <label htmlFor="languageSelection" className="block mb-2 text-gray-600 font-medium text-sm">
            Language Pair
          </label>
          <select
            id="languageSelection"
            name="languageSelection"
            value={formData.languageSelection}
            onChange={handleSelectChange}
            className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          >
            {Object.values(LanguageSelection).map((value) => (
              <option key={value} value={value}>
                {LANGUAGE_SELECTION_LABELS[value]}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-5">
          <label htmlFor="audioUrl" className="block mb-2 text-gray-600 font-medium text-sm">Audio URL</label>
          <input
            type="url"
            id="audioUrl"
            name="audioUrl"
            value={formData.audioUrl}
            onChange={handleChange}
            placeholder="https://example.com/audio.mp3"
            className="w-full px-3 py-3 border border-gray-300 rounded-lg text-sm transition-colors focus:outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-200"
          />
        </div>

        {tags && tags.length > 0 && (
          <div className="mb-5">
            <label className="block mb-2 text-gray-600 font-medium text-sm">Tags</label>
            <div className="flex flex-wrap gap-3">
              {tags.map((tag) => (
                <label key={tag.id} className="flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.tagIds.includes(tag.id)}
                    onChange={() => handleTagToggle(tag.id)}
                    className="w-auto mr-2"
                  />
                  <span
                    className="tag-pill"
                    style={{ backgroundColor: tag.color || '#ddd' }}
                  >
                    {tag.name}
                  </span>
                </label>
              ))}
            </div>
          </div>
        )}

        <div className="flex gap-3 mt-5">
          <button type="submit" className="px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg text-sm font-semibold cursor-pointer transition-transform hover:-translate-y-0.5 shadow-md">
            {card ? 'Update Card' : 'Create Card'}
          </button>
          <button type="button" onClick={onCancel} className="px-6 py-3 bg-gray-100 text-gray-600 border border-gray-300 rounded-lg text-sm font-semibold cursor-pointer transition-colors hover:bg-gray-200">
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}

export default VocabularyForm
