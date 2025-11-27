import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { vocabularyAPI, tagAPI } from '../services/api';
import VocabularyTable from '../components/VocabularyTable';
import VocabularyForm from '../components/VocabularyForm';

const VocabularyTablePage = () => {
  const [cards, setCards] = useState([]);
  const [tags, setTags] = useState([]);
  const [showForm, setShowForm] = useState(false);
  const [editingCard, setEditingCard] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [loading, setLoading] = useState(true);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadCards();
    loadTags();
  }, []);

  const loadCards = async () => {
    try {
      const response = await vocabularyAPI.getAll();
      setCards(response.data);
    } catch (error) {
      console.error('Failed to load cards:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadTags = async () => {
    try {
      const response = await tagAPI.getAll();
      setTags(response.data);
    } catch (error) {
      console.error('Failed to load tags:', error);
    }
  };

  const handleSaveCard = async (cardData) => {
    try {
      if (editingCard) {
        await vocabularyAPI.update(editingCard.id, cardData);
      } else {
        await vocabularyAPI.create(cardData);
      }
      setShowForm(false);
      setEditingCard(null);
      loadCards();
    } catch (error) {
      console.error('Failed to save card:', error);
    }
  };

  const handleEditCard = (card) => {
    setEditingCard(card);
    setShowForm(true);
  };

  const handleDeleteCard = async (id) => {
    if (window.confirm('Are you sure you want to delete this card?')) {
      try {
        await vocabularyAPI.delete(id);
        setCards(prevCards => prevCards.filter(card => card.id !== id));
      } catch (error) {
        console.error('Failed to delete card:', error);
        loadCards();
      }
    }
  };

  const handleAddNew = () => {
    setEditingCard(null);
    setShowForm(true);
  };

  const handleOverlayClick = (e) => {
    if (e.target.classList.contains('form-modal-overlay')) {
      setShowForm(false);
      setEditingCard(null);
    }
  };

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
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
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
            searchTerm={searchTerm}
          />
        )}

        {showForm && (
          <div className="form-modal-overlay fixed inset-0 bg-black/50 flex justify-center items-center z-[1000]" onClick={handleOverlayClick}>
            <div className="bg-white rounded-lg p-8 max-w-[600px] w-[90%] max-h-[90vh] overflow-y-auto shadow-2xl animate-modal-slide max-md:w-[95%] max-md:p-5" onClick={(e) => e.stopPropagation()}>
              <VocabularyForm
                card={editingCard}
                tags={tags}
                onSave={handleSaveCard}
                onCancel={() => {
                  setShowForm(false);
                  setEditingCard(null);
                }}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default VocabularyTablePage;
