import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { vocabularyAPI, tagAPI } from '../services/api';
import VocabularyList from '../components/VocabularyList';
import VocabularyForm from '../components/VocabularyForm';
import './Dashboard.css';

const Dashboard = () => {
  const [cards, setCards] = useState([]);
  const [tags, setTags] = useState([]);
  const [dueCount, setDueCount] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [editingCard, setEditingCard] = useState(null);
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadCards();
    loadTags();
    loadDueCount();
  }, []);

  const loadCards = async () => {
    try {
      const response = await vocabularyAPI.getAll();
      setCards(response.data);
    } catch (error) {
      console.error('Failed to load cards:', error);
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

  const loadDueCount = async () => {
    try {
      const response = await vocabularyAPI.getDueCount();
      setDueCount(response.data);
    } catch (error) {
      console.error('Failed to load due count:', error);
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
      loadDueCount();
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
        loadCards();
        loadDueCount();
      } catch (error) {
        console.error('Failed to delete card:', error);
      }
    }
  };

  const handleStartReview = () => {
    navigate('/review');
  };

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>Simple Anki</h1>
        <div className="header-actions">
          <span className="user-info">Welcome, {user?.username}</span>
          <button onClick={logout} className="btn-secondary">Logout</button>
        </div>
      </header>

      <div className="dashboard-content">
        <div className="stats-section">
          <div className="stat-card">
            <h3>Total Cards</h3>
            <p className="stat-value">{cards.length}</p>
          </div>
          <div className="stat-card due">
            <h3>Cards Due</h3>
            <p className="stat-value">{dueCount}</p>
            {dueCount > 0 && (
              <button onClick={handleStartReview} className="btn-primary">
                Start Review
              </button>
            )}
          </div>
        </div>

        <div className="actions-section">
          <button
            onClick={() => {
              setEditingCard(null);
              setShowForm(!showForm);
            }}
            className="btn-primary"
          >
            {showForm ? 'Cancel' : 'Add New Card'}
          </button>
        </div>

        {showForm && (
          <VocabularyForm
            card={editingCard}
            tags={tags}
            onSave={handleSaveCard}
            onCancel={() => {
              setShowForm(false);
              setEditingCard(null);
            }}
          />
        )}

        <VocabularyList
          cards={cards}
          onEdit={handleEditCard}
          onDelete={handleDeleteCard}
        />
      </div>
    </div>
  );
};

export default Dashboard;
