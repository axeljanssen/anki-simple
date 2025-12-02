package com.anki.simple.vocabulary.mapper;

import com.anki.simple.tag.mapper.TagMapper;
import com.anki.simple.vocabulary.VocabularyCard;
import com.anki.simple.vocabulary.dto.VocabularyCardLeanResponse;
import com.anki.simple.vocabulary.dto.VocabularyCardRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {TagMapper.class})
public interface VocabularyCardMapper {

  @Mapping(target = "tags", source = "tags")
  VocabularyCardResponse toResponse(VocabularyCard card);

  VocabularyCardLeanResponse toLeanResponse(VocabularyCard card);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "reviewHistories", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastReviewed", ignore = true)
  @Mapping(target = "nextReview", ignore = true)
  @Mapping(target = "easeFactor", ignore = true)
  @Mapping(target = "intervalDays", ignore = true)
  @Mapping(target = "repetitions", ignore = true)
  VocabularyCard toEntity(VocabularyCardRequest request);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "tags", ignore = true)
  @Mapping(target = "reviewHistories", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "lastReviewed", ignore = true)
  @Mapping(target = "nextReview", ignore = true)
  @Mapping(target = "easeFactor", ignore = true)
  @Mapping(target = "intervalDays", ignore = true)
  @Mapping(target = "repetitions", ignore = true)
  void updateEntityFromRequest(VocabularyCardRequest request, @MappingTarget VocabularyCard card);
}
