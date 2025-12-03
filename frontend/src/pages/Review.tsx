import React, { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { vocabularyAPI, reviewAPI } from '@/services/api'
import type { VocabularyCard } from '@/types'
import { LANGUAGE_SELECTION_LABELS } from '@/types'
import { AxiosError } from 'axios'

const Review = (): React.JSX.Element => {
  const [dueCards, setDueCards] = useState<VocabularyCard[]>([])
  const [currentIndex, setCurrentIndex] = useState<number>(0)
  const [showAnswer, setShowAnswer] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(true)
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [showKeyboardHelp, setShowKeyboardHelp] = useState<boolean>(false)
  const navigate = useNavigate()

  const loadDueCards = useCallback(async (): Promise<void> => {
    try {
      const response = await vocabularyAPI.getDue()
      setDueCards(response.data)
      setLoading(false)
    } catch (error) {
      const axiosError = error as AxiosError
      console.error('Failed to load due cards:', axiosError)
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    // Load data on mount - legitimate use case for data fetching
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadDueCards()
  }, [loadDueCards])

  const handleReview = useCallback(async (quality: 0 | 1 | 2 | 3 | 4 | 5): Promise<void> => {
    const currentCard = dueCards[currentIndex]
    if (!currentCard || submitting) return

    setSubmitting(true)

    try {
      await reviewAPI.review({
        cardId: currentCard.id,
        quality,
      })

      // Small delay for user to see feedback
      await new Promise(resolve => setTimeout(resolve, 300))

      if (currentIndex < dueCards.length - 1) {
        setCurrentIndex(currentIndex + 1)
        setShowAnswer(false)
      } else {
        navigate('/dashboard')
      }
    } catch (error) {
      const axiosError = error as AxiosError
      console.error('Failed to review card:', axiosError)
    } finally {
      setSubmitting(false)
    }
  }, [dueCards, currentIndex, submitting, navigate])

  // Keyboard shortcuts for review
  useEffect(() => {
    const handleKeyPress = (event: KeyboardEvent): void => {
      // Ignore if user is typing in an input
      if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
        return
      }

      // Prevent action if currently submitting
      if (submitting) {
        return
      }

      switch (event.key) {
        case ' ':
        case 'Space':
          if (!showAnswer) {
            event.preventDefault()
            setShowAnswer(true)
          }
          break

        case '1':
          if (showAnswer) {
            event.preventDefault()
            handleReview(0)
          }
          break

        case '2':
          if (showAnswer) {
            event.preventDefault()
            handleReview(3)
          }
          break

        case '3':
          if (showAnswer) {
            event.preventDefault()
            handleReview(4)
          }
          break

        case '4':
          if (showAnswer) {
            event.preventDefault()
            handleReview(5)
          }
          break

        case 'Escape':
          event.preventDefault()
          navigate('/dashboard')
          break

        case '?':
          event.preventDefault()
          setShowKeyboardHelp(true)
          break
      }
    }

    window.addEventListener('keydown', handleKeyPress)
    return () => {
      window.removeEventListener('keydown', handleKeyPress)
    }
  }, [showAnswer, submitting, handleReview, navigate])

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-700 p-5">
        <div className="max-w-[600px] my-24 mx-auto bg-white py-15 px-5 rounded-2xl text-center text-2xl text-gray-600 shadow-2xl">
          Loading...
        </div>
      </div>
    )
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
    )
  }

  const currentCard = dueCards[currentIndex]
  if (!currentCard) {
    navigate('/dashboard')
    return <></>
  }

  const progress = ((currentIndex + 1) / dueCards.length) * 100

  return (
    <>
      {/* Skip link for keyboard navigation */}
      <a
        href="#review-content"
        className="sr-only focus:not-sr-only focus:absolute focus:top-4 focus:left-4 focus:z-50 focus:px-4 focus:py-2 focus:bg-blue-600 focus:text-white focus:rounded focus:shadow-lg"
      >
        Skip to review content
      </a>

      <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-700 p-5">
        <div className="max-w-[800px] mx-auto mb-5 flex items-center gap-5">
          <button
            onClick={() => navigate('/dashboard')}
            className="px-5 py-2.5 bg-white/20 text-white border border-white rounded-md cursor-pointer text-sm transition-colors duration-200 hover:bg-white/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2"
            aria-label="Return to dashboard"
            title="Return to dashboard (Press Escape)"
          >
            ← Back
          </button>
          <button
            onClick={() => setShowKeyboardHelp(true)}
            className="px-5 py-2.5 bg-white/20 text-white border border-white rounded-md cursor-pointer text-sm transition-colors duration-200 hover:bg-white/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2"
            aria-label="Show keyboard shortcuts"
            title="Keyboard shortcuts (?)"
          >
            ⌨️ Shortcuts
          </button>
          <div className="flex-1 text-white">
            <span className="block mb-2 font-semibold">
              {currentIndex + 1} / {dueCards.length}
            </span>
            <div
              role="progressbar"
              aria-valuenow={currentIndex + 1}
              aria-valuemin={1}
              aria-valuemax={dueCards.length}
              aria-label={`Review progress: ${currentIndex + 1} of ${dueCards.length} cards completed`}
              className="h-2 bg-white/30 rounded overflow-hidden"
            >
              <div className="h-full bg-white transition-all duration-300" style={{ width: `${progress}%` }}></div>
            </div>
            <div className="sr-only" role="status" aria-live="polite" aria-atomic="true">
              {submitting ? 'Submitting review...' : `Card ${currentIndex + 1} of ${dueCards.length}`}
            </div>
          </div>
        </div>

        <main
          id="review-content"
          className="max-w-[800px] mx-auto bg-white rounded-2xl p-15 shadow-2xl min-h-[500px] flex flex-col justify-center max-md:p-8"
          aria-label="Vocabulary card review area"
          tabIndex={-1}
        >
        <div className="text-center mb-5">
          {currentCard.languageSelection && (
            <span className="inline-block px-3 py-1 bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-xl text-xs font-medium">
              {LANGUAGE_SELECTION_LABELS[currentCard.languageSelection]}
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
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <button
                  onClick={() => handleReview(0)}
                  disabled={submitting}
                  className="flex flex-col items-center p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-red-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-red-500 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  title="Complete blackout (Press 1)"
                  aria-label="Rate as Again - Quality 0: Total blackout (Press 1)"
                >
                  <span className="text-2xl mb-1">❌</span>
                  <span className="text-base mb-1 font-bold">Again</span>
                  <span className="text-xs opacity-90 font-normal">Total blackout</span>
                  <span className="text-xs mt-1 opacity-75">(Press 1)</span>
                </button>
                <button
                  onClick={() => handleReview(3)}
                  disabled={submitting}
                  className="flex flex-col items-center p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-orange-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-orange-500 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  title="Correct with difficulty (Press 2)"
                  aria-label="Rate as Hard - Quality 3: Correct but difficult (Press 2)"
                >
                  <span className="text-2xl mb-1">😰</span>
                  <span className="text-base mb-1 font-bold">Hard</span>
                  <span className="text-xs opacity-90 font-normal">Difficult recall</span>
                  <span className="text-xs mt-1 opacity-75">(Press 2)</span>
                </button>
                <button
                  onClick={() => handleReview(4)}
                  disabled={submitting}
                  className="flex flex-col items-center p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-green-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-green-500 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  title="Correct with hesitation (Press 3)"
                  aria-label="Rate as Good - Quality 4: Correct with some hesitation (Press 3)"
                >
                  <span className="text-2xl mb-1">✅</span>
                  <span className="text-base mb-1 font-bold">Good</span>
                  <span className="text-xs opacity-90 font-normal">Some hesitation</span>
                  <span className="text-xs mt-1 opacity-75">(Press 3)</span>
                </button>
                <button
                  onClick={() => handleReview(5)}
                  disabled={submitting}
                  className="flex flex-col items-center p-5 border-none rounded-lg cursor-pointer transition-all duration-200 text-white font-semibold bg-blue-500 hover:-translate-y-1 hover:shadow-lg hover:shadow-black/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-blue-500 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                  title="Perfect response (Press 4)"
                  aria-label="Rate as Easy - Quality 5: Perfect recall (Press 4)"
                >
                  <span className="text-2xl mb-1">🎯</span>
                  <span className="text-base mb-1 font-bold">Easy</span>
                  <span className="text-xs opacity-90 font-normal">Perfect recall</span>
                  <span className="text-xs mt-1 opacity-75">(Press 4)</span>
                </button>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-center mt-10">
            <button
              onClick={() => setShowAnswer(true)}
              className="px-10 py-4 bg-gradient-to-br from-blue-600 to-blue-700 text-white border-none rounded-lg text-lg font-semibold cursor-pointer transition-transform duration-200 hover:scale-105 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
              aria-label="Reveal answer for vocabulary card"
              title="Reveal answer (Press Space)"
            >
              Show Answer
            </button>
          </div>
        )}
        </main>

        {/* Keyboard Shortcuts Help Modal */}
        {showKeyboardHelp && (
          <div
            className="fixed inset-0 bg-black/50 flex items-center justify-center p-5 z-50"
            onClick={() => setShowKeyboardHelp(false)}
          >
            <div
              className="bg-white rounded-xl p-8 max-w-md w-full shadow-2xl animate-modal-slide"
              onClick={(e) => e.stopPropagation()}
              role="dialog"
              aria-labelledby="keyboard-help-title"
              aria-modal="true"
            >
              <h2 id="keyboard-help-title" className="text-2xl font-bold text-gray-800 mb-6">
                Keyboard Shortcuts
              </h2>

              <div className="space-y-3 text-gray-700">
                <div className="flex justify-between items-center">
                  <span className="font-medium">Show Answer</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">Space</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Again (Total blackout)</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">1</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Hard (Difficult recall)</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">2</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Good (Some hesitation)</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">3</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Easy (Perfect recall)</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">4</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Back to Dashboard</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">Esc</kbd>
                </div>
                <div className="flex justify-between items-center">
                  <span className="font-medium">Show this help</span>
                  <kbd className="px-3 py-1 bg-gray-100 border border-gray-300 rounded text-sm font-mono">?</kbd>
                </div>
              </div>

              <button
                onClick={() => setShowKeyboardHelp(false)}
                className="mt-6 w-full px-6 py-3 bg-gradient-to-br from-blue-600 to-blue-700 text-white rounded-lg font-semibold hover:-translate-y-0.5 transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-600 focus-visible:ring-offset-2"
              >
                Got it!
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}

export default Review
