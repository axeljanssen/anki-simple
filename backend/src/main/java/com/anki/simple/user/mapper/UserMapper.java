package com.anki.simple.user.mapper;

import com.anki.simple.user.User;
import com.anki.simple.user.dto.AuthResponse;
import com.anki.simple.user.dto.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "vocabularyCards", ignore = true)
  User toEntity(SignupRequest request);

  default AuthResponse toAuthResponse(User user, String token) {
    return new AuthResponse(token, user.getUsername(), user.getEmail());
  }
}
