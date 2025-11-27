import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { vocabularyAPI, reviewAPI } from '../services/api';

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
      <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-700 p-5">
        <div className="max-w-[600px] my-24 mx-auto bg-white py-15 px-5 rounded-2xl text-center text-2xl text-gray-600 shadow-2xl">
          Loading...
        </div>
      </div>
    );
  }

  if (dueCards.length === 0) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-700 p-5">
        <div className="max-w-[600px] my-24 mx-auto bg-white py-15 px-5 rounded-2xl text-center shadow-2xl">
          <h2 className="text-gray-800 mb-4">No cards due for review!</h2>
          <p className="text-gray-600 mb-8">Come back later or add more vocabulary cards.</p>
          <button
            onClick={() => navigate('/dashboard')}
            className="px-10 py-4 bg-gradient-to-br from-blue-600 to-blue-700 text-white border-none rounded-lg text-lg font-semibold cursor-pointer transition-transform duration-200 hover:scale-105"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const currentCard = dueCards[currentIndex];
  const progress = ((currentIndex + 1) / dueCards.length) * 100;

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-700 p-5">
      <div className="max-w-[800px] mx-auto mb-5 flex items-center gap-5">
        <button
          onClick={() => navigate('/dashboard')}
          className="px-5 py-2.5 bg-white/20 text-white border border-white rounded-md cursor-pointer text-sm transition-colors duration-200 hover:bg-white/30"
        >
          ← Back
        </button>
        <div className="flex-1 text-white">
          <span className="block mb-2 font-semibold">
            {currentIndex + 1} / {dueCards.length}
          </span>
          <div className="h-2 bg-white/30 rounded overflow-hidden">
            <div className="h-full bg-white transition-all duration-300" style={{ width: `${progress}%` }}></div>
          </div>
        </div>
      </div>

      <div className="max-w-[800px] mx-auto bg-white rounded-2xl p-15 shadow-2xl min-h-[500px] flex flex-col justify-center max-md:p-8">
        <div className="text-center mb-5">
          {currentCard.sourceLanguage && currentCard.targetLanguage && (
            <span className="inline-block px-3 py-1 bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-xl text-xs font-medium">
              {currentCard.sourceLanguage} → {currentCard.targetLanguage}
            </span>
          )}
        </div>

        <div className="text-center my-10">
          <h2 className="text-4xl text-gray-800 m-0 max-md:text-2xl">{currentCard.front}</h2>
        </div>

        {showAnswer ? (
          <div className="mt-8">
            <div className="text-center mb-8">
              <h3 className="text-gray-600 text-lg mb-4">Answer:</h3>
              <p className="text-3xl text-gray-800 font-semibold">{currentCard.back}</p>
            </div>

            {currentCard.exampleSentence && (
              <div className="bg-gray-50 p-5 rounded-lg my-5">
                <h4 className="m-0 mb-2.5 text-gray-600 text-sm">Example:</h4>
                <p className="m-0 text-gray-800 italic">{currentCard.exampleSentence}</p>
              </div>
            )}

            {currentCard.audioUrl && (
              <div className="text-center my-5">
                <audio controls src={currentCard.audioUrl}>
                  Your browser does not support the audio element.
                </audio>
              </div>
            )}

            <div className="mt-10">
              <p className="text-center text-gray-600 mb-5 text-base">How well did you know this?</p>
              <div className="grid grid-cols-4 gap-4 max-md:grid-cols-2">
                <button
                  onClick={() => handleReview(0)}
                  className="flex flex-col p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-red-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20"
                  title="Complete blackout"
                >
                  <span className="text-base mb-2">Again</span>
                  <span className="text-xs opacity-90 font-normal">Total blackout</span>
                </button>
                <button
                  onClick={() => handleReview(3)}
                  className="flex flex-col p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-orange-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20"
                  title="Correct with difficulty"
                >
                  <span className="text-base mb-2">Hard</span>
                  <span className="text-xs opacity-90 font-normal">Difficult recall</span>
                </button>
                <button
                  onClick={() => handleReview(4)}
                  className="flex flex-col p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-green-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20"
                  title="Correct with hesitation"
                >
                  <span className="text-base mb-2">Good</span>
                  <span className="text-xs opacity-90 font-normal">Some hesitation</span>
                </button>
                <button
                  onClick={() => handleReview(5)}
                  className="flex flex-col p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-blue-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20"
                  title="Perfect response"
                >
                  <span className="text-base mb-2">Easy</span>
                  <span className="text-xs opacity-90 font-normal">Perfect recall</span>
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center mt-10">
            <button
              onClick={() => setShowAnswer(true)}
              className="px-10 py-4 bg-gradient-to-br from-blue-600 to-blue-700 text-white border-none rounded-lg text-lg font-semibold cursor-pointer transition-transform duration-200 hover:scale-105"
            >
              Show Answer
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default Review;
