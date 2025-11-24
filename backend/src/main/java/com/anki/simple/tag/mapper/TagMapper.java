package com.anki.simple.tag.mapper;

import com.anki.simple.tag.Tag;
import com.anki.simple.tag.dto.TagRequest;
import com.anki.simple.tag.dto.TagResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {

  TagResponse toResponse(Tag tag);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "user", ignore = true)
  @Mapping(target = "cards", ignore = true)
  Tag toEntity(TagRequest request);
}
