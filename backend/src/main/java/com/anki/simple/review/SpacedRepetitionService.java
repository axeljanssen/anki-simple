package com.anki.simple.review;

import com.anki.simple.vocabulary.VocabularyCard;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {

    /**
     * SM-2 Algorithm implementation
     * quality: 0-5 rating
     * 0: complete blackout
     * 1: incorrect response, but familiar
     * 2: incorrect response, seems easy to recall
     * 3: correct response, but difficult
     * 4: correct response, some hesitation
     * 5: perfect response
     */
    public void updateCardSchedule(VocabularyCard card, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5");
        }

        double easeFactor = card.getEaseFactor();
        int repetitions = card.getRepetitions();
        int interval = card.getIntervalDays();

        if (quality >= 3) {
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }
            repetitions++;
        } else {
            repetitions = 0;
            interval = 1;
        }

        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

        if (easeFactor < 1.3) {
            easeFactor = 1.3;
        }

        card.setEaseFactor(easeFactor);
        card.setRepetitions(repetitions);
        card.setIntervalDays(interval);
        card.setLastReviewed(LocalDateTime.now());
        card.setNextReview(LocalDateTime.now().plusDays(interval));
    }
}
