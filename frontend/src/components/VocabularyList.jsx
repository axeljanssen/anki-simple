import './VocabularyList.css';

const VocabularyList = ({ cards, onEdit, onDelete }) => {
  if (cards.length === 0) {
    return (
      <div className="empty-state">
        <p>No vocabulary cards yet. Create your first card to get started!</p>
      </div>
    );
  }

  return (
    <div className="vocabulary-list">
      <h2>Your Vocabulary Cards</h2>
      <div className="cards-grid">
        {cards.map((card) => (
          <div key={card.id} className="vocabulary-card">
            <div className="card-content">
              <div className="card-front">
                <strong>Front:</strong> {card.front}
              </div>
              <div className="card-back">
                <strong>Back:</strong> {card.back}
              </div>
              {card.exampleSentence && (
                <div className="card-example">
                  <strong>Example:</strong> {card.exampleSentence}
                </div>
              )}
              <div className="card-languages">
                {card.sourceLanguage && card.targetLanguage && (
                  <span className="language-badge">
                    {card.sourceLanguage} → {card.targetLanguage}
                  </span>
                )}
              </div>
              {card.tags && card.tags.length > 0 && (
                <div className="card-tags">
                  {card.tags.map((tag) => (
                    <span
                      key={tag.id}
                      className="tag"
                      style={{ backgroundColor: tag.color || '#ddd' }}
                    >
                      {tag.name}
                    </span>
                  ))}
                </div>
              )}
              <div className="card-stats">
                <small>
                  Ease: {card.easeFactor?.toFixed(2)} | Interval: {card.intervalDays} days
                </small>
              </div>
            </div>
            <div className="card-actions">
              <button onClick={() => onEdit(card)} className="btn-edit">
                Edit
              </button>
              <button onClick={() => onDelete(card.id)} className="btn-delete">
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default VocabularyList;
