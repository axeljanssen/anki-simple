package com.anki.simple.tag;

import com.anki.simple.exception.TagAlreadyExistsException;
import com.anki.simple.exception.TagNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.dto.TagRequest;
import com.anki.simple.tag.dto.TagResponse;
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

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    @Transactional
    public TagResponse createTag(TagRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (tagRepository.findByNameAndUserId(request.getName(), user.getId()).isPresent()) {
            throw new TagAlreadyExistsException(request.getName());
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setUser(user);

        Tag savedTag = tagRepository.save(tag);
        return new TagResponse(savedTag.getId(), savedTag.getName(), savedTag.getColor());
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return tagRepository.findByUserId(user.getId())
                .stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColor()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteTag(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException(id));

        if (!tag.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to tag");
        }

        tagRepository.delete(tag);
    }
}
