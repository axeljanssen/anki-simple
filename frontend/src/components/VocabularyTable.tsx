import React from 'react'
import type { VocabularyTableProps, SortableField } from '@/types'
import { LANGUAGE_SELECTION_LABELS } from '@/types'

const VocabularyTable = ({
  cards,
  onEdit,
  onDelete,
  sortBy,
  sortDirection,
  onSort,
  loading = false
}: VocabularyTableProps): React.JSX.Element => {
  const getSortIcon = (field: SortableField): string => {
    if (sortBy !== field) return '↕'
    return sortDirection === 'asc' ? '↑' : '↓'
  }

  const handleSortClick = (field: SortableField): void => {
    if (!loading) {
      onSort(field)
    }
  }

  if (cards.length === 0) {
    return (
      <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center">
        <p className="text-gray-600 text-base m-0">No vocabulary cards yet. Create your first card to get started!</p>
      </div>
    )
  }

  return (
    <div className="bg-white p-8 rounded-lg shadow-sm">
      <div className="mb-4 text-gray-600 text-sm">
        <span>Showing {cards.length} cards</span>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full border-collapse min-w-[800px]">
          <thead>
            <tr>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">
                <div className="flex items-center gap-2">
                  <span>Front</span>
                  <button
                    onClick={() => handleSortClick('front')}
                    disabled={loading}
                    className={`text-lg leading-none ${loading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:text-blue-600'}`}
                    aria-label="Sort by front"
                  >
                    {getSortIcon('front')}
                  </button>
                </div>
              </th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">
                <div className="flex items-center gap-2">
                  <span>Back</span>
                  <button
                    onClick={() => handleSortClick('back')}
                    disabled={loading}
                    className={`text-lg leading-none ${loading ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:text-blue-600'}`}
                    aria-label="Sort by back"
                  >
                    {getSortIcon('back')}
                  </button>
                </div>
              </th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Languages</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Tags</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-center font-semibold text-gray-800 border-b-2 border-gray-200 w-[150px]">Actions</th>
            </tr>
          </thead>
          <tbody>
            {cards.map((card) => (
              <tr key={card.id} className="hover:bg-gray-50 last:border-b-0">
                <td className="px-4 py-3 border-b border-gray-200 align-middle">{card.front}</td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle">{card.back}</td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle">
                  {card.languageSelection ? (
                    <span className="inline-block px-3 py-1 bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-xl text-xs font-medium">
                      {LANGUAGE_SELECTION_LABELS[card.languageSelection]}
                    </span>
                  ) : (
                    '-'
                  )}
                </td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle">
                  {card.tags && card.tags.length > 0 ? (
                    <div className="flex flex-wrap gap-1.5">
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
                  ) : (
                    '-'
                  )}
                </td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle text-center">
                  <button
                    onClick={() => onEdit(card)}
                    className="mx-1 px-3 py-1.5 text-sm border-none rounded-md cursor-pointer font-medium bg-green-500 text-white transition-all duration-300 hover:bg-green-600 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-green-500/30"
                  >
                    Edit
                  </button>
                  <button
                    onClick={() => onDelete(card.id)}
                    className="mx-1 px-3 py-1.5 text-sm border-none rounded-md cursor-pointer font-medium bg-red-500 text-white transition-all duration-300 hover:bg-red-600 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-red-500/30"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default VocabularyTable
