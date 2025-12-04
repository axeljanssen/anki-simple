package com.anki.simple.tag;

import com.anki.simple.exception.TagAlreadyExistsException;
import com.anki.simple.exception.TagNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.dto.TagRequest;
import com.anki.simple.tag.dto.TagResponse;
import com.anki.simple.tag.mapper.TagMapper;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    public static final String USER_NOT_FOUND = "User not found";
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final TagMapper tagMapper;

    @Transactional
    public TagResponse createTag(TagRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        if (tagRepository.findByNameAndUserId(request.getName(), user.getId()).isPresent()) {
            throw new TagAlreadyExistsException(request.getName());
        }

        Tag tag = tagMapper.toEntity(request);
        tag.setUser(user);

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponse(savedTag);
    }

    @Transactional
    public TagResponse updateTag(Long id, TagRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to tag");
        }

        // Check for duplicate name (excluding current tag)
        tagRepository.findByNameAndUserId(request.getName(), user.getId())
                .ifPresent(existingTag -> {
                    if (!existingTag.getId().equals(id)) {
                        throw new TagAlreadyExistsException(request.getName());
                    }
                });

        tag.setName(request.getName());
        tag.setColor(request.getColor());

        Tag updatedTag = tagRepository.save(tag);
        return tagMapper.toResponse(updatedTag);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        return tagRepository.findByUserId(user.getId())
                .stream()
                .map(tagMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTag(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to tag");
        }

        tagRepository.delete(tag);
    }
}
