package com.anki.simple.vocabulary;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.Tag;
import com.anki.simple.tag.TagRepository;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import com.anki.simple.vocabulary.mapper.VocabularyCardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final VocabularyCardMapper vocabularyCardMapper;

    @Transactional
    public VocabularyCardResponse createCard(VocabularyCardRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VocabularyCard card = vocabularyCardMapper.toEntity(request);
        card.setUser(user);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            card.setTags(tags);
        }

        VocabularyCard savedCard = vocabularyRepository.save(card);
        return vocabularyCardMapper.toResponse(savedCard);
    }

    @Transactional(readOnly = true)
    public List<VocabularyCardResponse> getAllCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return vocabularyRepository.findByUserId(user.getId())
                .stream()
                .map(vocabularyCardMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyCardResponse> getDueCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return vocabularyRepository.findDueCards(user.getId(), LocalDateTime.now())
                .stream()
                .map(vocabularyCardMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getDueCardsCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return vocabularyRepository.countByUserIdAndNextReviewBefore(user.getId(), LocalDateTime.now());
    }

    @Transactional
    public VocabularyCardResponse updateCard(Long id, VocabularyCardRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VocabularyCard card = vocabularyRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to card");
        }

        vocabularyCardMapper.updateEntityFromRequest(request, card);

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            card.setTags(tags);
        }

        VocabularyCard updatedCard = vocabularyRepository.save(card);
        return vocabularyCardMapper.toResponse(updatedCard);
    }

    @Transactional
    public void deleteCard(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VocabularyCard card = vocabularyRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException(id));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Unauthorized access to card");
        }

        vocabularyRepository.delete(card);
    }
}
