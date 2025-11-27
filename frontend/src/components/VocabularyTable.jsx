import { useMemo } from 'react';

const VocabularyTable = ({ cards, onEdit, onDelete, searchTerm }) => {
  const filteredCards = useMemo(() => {
    if (!searchTerm) {
      return cards;
    }

    const term = searchTerm.toLowerCase();
    return cards.filter(card =>
      card.front.toLowerCase().includes(term) ||
      card.back.toLowerCase().includes(term) ||
      (card.exampleSentence && card.exampleSentence.toLowerCase().includes(term))
    );
  }, [cards, searchTerm]);

  if (cards.length === 0) {
    return (
      <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center">
        <p className="text-gray-600 text-base m-0">No vocabulary cards yet. Create your first card to get started!</p>
      </div>
    );
  }

  if (filteredCards.length === 0) {
    return (
      <div className="bg-white py-15 px-5 rounded-lg shadow-sm text-center">
        <p className="text-gray-600 text-base m-0">No cards match your search: &quot;{searchTerm}&quot;</p>
      </div>
    );
  }

  return (
    <div className="bg-white p-8 rounded-lg shadow-sm">
      <div className="mb-4 text-gray-600 text-sm">
        <span>Showing {filteredCards.length} of {cards.length} cards</span>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full border-collapse min-w-[800px]">
          <thead>
            <tr>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Front</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Back</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Languages</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-left font-semibold text-gray-800 border-b-2 border-gray-200">Tags</th>
              <th scope="col" className="bg-gray-100 px-4 py-3 text-center font-semibold text-gray-800 border-b-2 border-gray-200 w-[150px]">Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredCards.map((card) => (
              <tr key={card.id} className="hover:bg-gray-50 last:border-b-0">
                <td className="px-4 py-3 border-b border-gray-200 align-middle">{card.front}</td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle">{card.back}</td>
                <td className="px-4 py-3 border-b border-gray-200 align-middle">
                  {card.sourceLanguage && card.targetLanguage ? (
                    <span className="inline-block px-3 py-1 bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-xl text-xs font-medium">
                      {card.sourceLanguage} → {card.targetLanguage}
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
  );
};

export default VocabularyTable;
