import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { vocabularyAPI, reviewAPI } from '../services/api';
import './Review.css';

const Review = () => {
  const [dueCards, setDueCards] = useState([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showAnswer, setShowAnswer] = useState(false);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadDueCards();
  }, []);

  const loadDueCards = async () => {
    try {
      const response = await vocabularyAPI.getDue();
      setDueCards(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to load due cards:', error);
      setLoading(false);
    }
  };

  const handleReview = async (quality) => {
    const currentCard = dueCards[currentIndex];

    try {
      await reviewAPI.review({
        cardId: currentCard.id,
        quality: quality,
      });

      if (currentIndex < dueCards.length - 1) {
        setCurrentIndex(currentIndex + 1);
        setShowAnswer(false);
      } else {
        navigate('/dashboard');
      }
    } catch (error) {
      console.error('Failed to review card:', error);
    }
  };

  if (loading) {
    return (
      <div className="review-container">
        <div className="loading">Loading...</div>
      </div>
    );
  }

  if (dueCards.length === 0) {
    return (
      <div className="review-container">
        <div className="empty-review">
          <h2>No cards due for review!</h2>
          <p>Come back later or add more vocabulary cards.</p>
          <button onClick={() => navigate('/dashboard')} className="btn-primary">
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const currentCard = dueCards[currentIndex];
  const progress = ((currentIndex + 1) / dueCards.length) * 100;

  return (
    <div className="review-container">
      <div className="review-header">
        <button onClick={() => navigate('/dashboard')} className="btn-back">
          ← Back
        </button>
        <div className="review-progress">
          <span>
            {currentIndex + 1} / {dueCards.length}
          </span>
          <div className="progress-bar">
            <div className="progress-fill" style={{ width: `${progress}%` }}></div>
          </div>
        </div>
      </div>

      <div className="review-card">
        <div className="card-languages">
          {currentCard.sourceLanguage && currentCard.targetLanguage && (
            <span className="language-badge">
              {currentCard.sourceLanguage} → {currentCard.targetLanguage}
            </span>
          )}
        </div>

        <div className="card-question">
          <h2>{currentCard.front}</h2>
        </div>

        {showAnswer ? (
          <div className="card-answer-section">
            <div className="card-answer">
              <h3>Answer:</h3>
              <p>{currentCard.back}</p>
            </div>

            {currentCard.exampleSentence && (
              <div className="card-example">
                <h4>Example:</h4>
                <p>{currentCard.exampleSentence}</p>
              </div>
            )}

            {currentCard.audioUrl && (
              <div className="card-audio">
                <audio controls src={currentCard.audioUrl}>
                  Your browser does not support the audio element.
                </audio>
              </div>
            )}

            <div className="rating-section">
              <p className="rating-instruction">How well did you know this?</p>
              <div className="rating-buttons">
                <button
                  onClick={() => handleReview(0)}
                  className="rating-btn fail"
                  title="Complete blackout"
                >
                  <span className="rating-label">Again</span>
                  <span className="rating-desc">Total blackout</span>
                </button>
                <button
                  onClick={() => handleReview(3)}
                  className="rating-btn hard"
                  title="Correct with difficulty"
                >
                  <span className="rating-label">Hard</span>
                  <span className="rating-desc">Difficult recall</span>
                </button>
                <button
                  onClick={() => handleReview(4)}
                  className="rating-btn good"
                  title="Correct with hesitation"
                >
                  <span className="rating-label">Good</span>
                  <span className="rating-desc">Some hesitation</span>
                </button>
                <button
                  onClick={() => handleReview(5)}
                  className="rating-btn easy"
                  title="Perfect response"
                >
                  <span className="rating-label">Easy</span>
                  <span className="rating-desc">Perfect recall</span>
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="show-answer-section">
            <button onClick={() => setShowAnswer(true)} className="btn-show-answer">
              Show Answer
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Review;
