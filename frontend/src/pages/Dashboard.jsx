import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { vocabularyAPI } from '../services/api';

const Dashboard = () => {
  const [cards, setCards] = useState([]);
  const [dueCount, setDueCount] = useState(0);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const loadCards = async () => {
    try {
      const response = await vocabularyAPI.getAll();
      setCards(response.data);
    } catch (error) {
      console.error('Failed to load cards:', error);
    }
  };

  const loadDueCount = async () => {
    try {
      const response = await vocabularyAPI.getDueCount();
      setDueCount(response.data);
    } catch (error) {
      console.error('Failed to load due count:', error);
    }
  };

  const handleStartReview = () => {
    navigate('/review');
  };

  useEffect(() => {
    loadCards();
    loadDueCount();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-gradient-to-r from-blue-600 to-blue-700 text-white px-10 py-5 flex justify-between items-center shadow-md">
        <h1 className="m-0 text-2xl font-semibold">Simple Anki</h1>
        <div className="flex items-center gap-5">
          <button onClick={() => navigate('/vocabulary-table')} className="px-4 py-2 bg-white/20 text-white border border-white rounded-lg text-sm cursor-pointer transition-colors hover:bg-white/30">
            Table View
          </button>
          <span className="text-sm">Welcome, {user?.username}</span>
          <button onClick={logout} className="px-4 py-2 bg-white/20 text-white border border-white rounded-lg text-sm cursor-pointer transition-colors hover:bg-white/30">Logout</button>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-5 py-10">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mb-8">
          <div className="bg-white p-8 rounded-xl shadow-sm text-center">
            <h3 className="m-0 mb-4 text-gray-600 text-base font-medium">Total Cards</h3>
            <p className="text-5xl font-bold text-gray-800 m-0">{cards.length}</p>
          </div>
          <div className="bg-white p-8 rounded-xl shadow-sm text-center">
            <h3 className="m-0 mb-4 text-gray-600 text-base font-medium">Cards Due</h3>
            <p className="text-5xl font-bold text-blue-600 m-0">{dueCount}</p>
            {dueCount > 0 && (
              <button onClick={handleStartReview} className="mt-4 px-6 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-lg text-sm font-semibold cursor-pointer transition-transform hover:-translate-y-0.5 shadow-lg">
                Start Review
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
