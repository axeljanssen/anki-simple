import { useState, useEffect } from 'react';
import './VocabularyForm.css';

const VocabularyForm = ({ card, tags, onSave, onCancel }) => {
  const [formData, setFormData] = useState({
    front: '',
    back: '',
    exampleSentence: '',
    sourceLanguage: '',
    targetLanguage: '',
    audioUrl: '',
    tagIds: [],
  });

  useEffect(() => {
    if (card) {
      setFormData({
        front: card.front || '',
        back: card.back || '',
        exampleSentence: card.exampleSentence || '',
        sourceLanguage: card.sourceLanguage || '',
        targetLanguage: card.targetLanguage || '',
        audioUrl: card.audioUrl || '',
        tagIds: card.tags ? card.tags.map((t) => t.id) : [],
      });
    }
  }, [card]);

  const handleSubmit = (e) => {
    e.preventDefault();
    onSave(formData);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleTagToggle = (tagId) => {
    setFormData((prev) => ({
      ...prev,
      tagIds: prev.tagIds.includes(tagId)
        ? prev.tagIds.filter((id) => id !== tagId)
        : [...prev.tagIds, tagId],
    }));
  };

  return (
    <div className="vocabulary-form">
      <h3>{card ? 'Edit Card' : 'Create New Card'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="front">Front (Word/Question) *</label>
            <input
              type="text"
              id="front"
              name="front"
              value={formData.front}
              onChange={handleChange}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="back">Back (Translation/Answer) *</label>
            <input
              type="text"
              id="back"
              name="back"
              value={formData.back}
              onChange={handleChange}
              required
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="exampleSentence">Example Sentence</label>
          <textarea
            id="exampleSentence"
            name="exampleSentence"
            value={formData.exampleSentence}
            onChange={handleChange}
            rows="3"
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="sourceLanguage">Source Language</label>
            <input
              type="text"
              id="sourceLanguage"
              name="sourceLanguage"
              value={formData.sourceLanguage}
              onChange={handleChange}
              placeholder="e.g., EN"
            />
          </div>
          <div className="form-group">
            <label htmlFor="targetLanguage">Target Language</label>
            <input
              type="text"
              id="targetLanguage"
              name="targetLanguage"
              value={formData.targetLanguage}
              onChange={handleChange}
              placeholder="e.g., ES"
            />
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="audioUrl">Audio URL</label>
          <input
            type="url"
            id="audioUrl"
            name="audioUrl"
            value={formData.audioUrl}
            onChange={handleChange}
            placeholder="https://example.com/audio.mp3"
          />
        </div>

        {tags && tags.length > 0 && (
          <div className="form-group">
            <label>Tags</label>
            <div className="tags-selection">
              {tags.map((tag) => (
                <label key={tag.id} className="tag-checkbox">
                  <input
                    type="checkbox"
                    checked={formData.tagIds.includes(tag.id)}
                    onChange={() => handleTagToggle(tag.id)}
                  />
                  <span
                    className="tag-label"
                    style={{ backgroundColor: tag.color || '#ddd' }}
                  >
                    {tag.name}
                  </span>
                </label>
              ))}
            </div>
          </div>
        )}

        <div className="form-actions">
          <button type="submit" className="btn-primary">
            {card ? 'Update Card' : 'Create Card'}
          </button>
          <button type="button" onClick={onCancel} className="btn-cancel">
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default VocabularyForm;
