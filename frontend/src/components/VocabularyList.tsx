import React from 'react'
import type { VocabularyListProps } from '@/types'
import { LANGUAGE_SELECTION_LABELS } from '@/types'

const VocabularyList = ({ cards, onEdit, onDelete }: VocabularyListProps): React.JSX.Element => {
  if (cards.length === 0) {
    return (
      <div className="text-center py-16 px-5 text-gray-400">
        <p>No vocabulary cards yet. Create your first card to get started!</p>
      </div>
    )
  }

  return (
    <div className="bg-white p-8 rounded-xl shadow-md">
      <h2 className="mt-0 mb-5 text-gray-800 text-xl font-semibold">Your Vocabulary Cards</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {cards.map((card) => (
          <div key={card.id} className="border border-gray-200 rounded-lg p-5 bg-gray-50 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg">
            <div className="mb-4">
              <div className="mb-2.5 leading-relaxed">
                <strong>Front:</strong> {card.front}
              </div>
              <div className="mb-2.5 leading-relaxed">
                <strong>Back:</strong> {card.back}
              </div>
              {card.exampleSentence && (
                <div className="mb-2.5 leading-relaxed">
                  <strong>Example:</strong> {card.exampleSentence}
                </div>
              )}
              <div className="my-2.5">
                {card.languageSelection && (
                  <span className="inline-block px-3 py-1 bg-blue-50 text-blue-600 rounded-full text-xs font-medium">
                    {LANGUAGE_SELECTION_LABELS[card.languageSelection]}
                  </span>
                )}
              </div>
              {card.tags && card.tags.length > 0 && (
                <div className="flex flex-wrap gap-2 my-2.5">
                  {card.tags.map((tag) => (
                    <span
                      key={tag.id}
                      className="tag-pill"
                      style={{ backgroundColor: tag.color || '#ddd' }}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              )}
              <div className="mt-2.5 text-gray-600 text-sm">
                <small>
                  Ease: {card.easeFactor?.toFixed(2)} | Interval: {card.intervalDays} days
                </small>
              </div>
            </div>
            <div className="flex gap-2.5">
              <button onClick={() => onEdit(card)} className="flex-1 px-4 py-2 bg-green-500 text-white rounded-lg text-sm cursor-pointer transition-opacity hover:opacity-90">
                Edit
              </button>
              <button onClick={() => onDelete(card.id)} className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg text-sm cursor-pointer transition-opacity hover:opacity-90">
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

export default VocabularyList
