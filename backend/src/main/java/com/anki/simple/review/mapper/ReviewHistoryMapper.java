package com.anki.simple.review.mapper;

import com.anki.simple.review.ReviewHistory;
import com.anki.simple.vocabulary.VocabularyCard;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewHistoryMapper {

  default ReviewHistory createFromCardAndQuality(VocabularyCard card, int quality) {
    ReviewHistory history = new ReviewHistory();
    history.setCard(card);
    history.setQuality(quality);
    history.setEaseFactor(card.getEaseFactor());
    history.setIntervalDays(card.getIntervalDays());
    return history;
  }
}
