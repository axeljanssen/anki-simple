package com.anki.simple.review;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.review.dto.ReviewRequest;
import com.anki.simple.review.mapper.ReviewHistoryMapper;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.VocabularyRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import com.anki.simple.vocabulary.mapper.VocabularyCardMapper;
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
    private final VocabularyCardMapper vocabularyCardMapper;
    private final ReviewHistoryMapper reviewHistoryMapper;

    @Transactional
    public VocabularyCardResponse reviewCard(ReviewRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VocabularyCard card = vocabularyRepository.findById(request.getCardId())
                .orElseThrow(() -> new CardNotFoundException(request.getCardId()));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to card");
        }

        spacedRepetitionService.updateCardSchedule(card, request.getQuality());

        ReviewHistory history = reviewHistoryMapper.createFromCardAndQuality(card, request.getQuality());
        reviewHistoryRepository.save(history);

        VocabularyCard updatedCard = vocabularyRepository.save(card);

        return vocabularyCardMapper.toResponse(updatedCard);
    }
}
