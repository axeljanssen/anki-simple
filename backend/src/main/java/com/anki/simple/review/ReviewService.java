package com.anki.simple.review;

import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.VocabularyRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import com.anki.simple.vocabulary.VocabularyService;
import com.anki.simple.review.dto.ReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final VocabularyRepository vocabularyRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final UserRepository userRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    @Transactional
    public VocabularyCardResponse reviewCard(ReviewRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        VocabularyCard card = vocabularyRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        spacedRepetitionService.updateCardSchedule(card, request.getQuality());

        ReviewHistory history = new ReviewHistory();
        history.setCard(card);
        history.setQuality(request.getQuality());
        history.setEaseFactor(card.getEaseFactor());
        history.setIntervalDays(card.getIntervalDays());
        reviewHistoryRepository.save(history);

        VocabularyCard updatedCard = vocabularyRepository.save(card);

        return mapToResponse(updatedCard);
    }

    private VocabularyCardResponse mapToResponse(VocabularyCard card) {
        return new VocabularyCardResponse(
                card.getId(),
                card.getFront(),
                card.getBack(),
                card.getExampleSentence(),
                card.getSourceLanguage(),
                card.getTargetLanguage(),
                card.getAudioUrl(),
                card.getCreatedAt(),
                card.getLastReviewed(),
                card.getNextReview(),
                card.getEaseFactor(),
                card.getIntervalDays(),
                card.getRepetitions(),
                null
        );
    }
}
