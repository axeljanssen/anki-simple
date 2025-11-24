package com.anki.simple.vocabulary;

import com.anki.simple.exception.CardNotFoundException;
import com.anki.simple.exception.UnauthorizedException;
import com.anki.simple.exception.UserNotFoundException;
import com.anki.simple.tag.Tag;
import com.anki.simple.tag.TagRepository;
import com.anki.simple.tag.dto.TagResponse;
import com.anki.simple.user.User;
import com.anki.simple.user.UserRepository;
import com.anki.simple.vocabulary.dto.VocabularyCardRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
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

    @Transactional
    public VocabularyCardResponse createCard(VocabularyCardRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VocabularyCard card = new VocabularyCard();
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setExampleSentence(request.getExampleSentence());
        card.setSourceLanguage(request.getSourceLanguage());
        card.setTargetLanguage(request.getTargetLanguage());
        card.setAudioUrl(request.getAudioUrl());
        card.setUser(user);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            card.setTags(tags);
        }

        VocabularyCard savedCard = vocabularyRepository.save(card);
        return mapToResponse(savedCard);
    }

    @Transactional(readOnly = true)
    public List<VocabularyCardResponse> getAllCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return vocabularyRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VocabularyCardResponse> getDueCards(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return vocabularyRepository.findDueCards(user.getId(), LocalDateTime.now())
                .stream()
                .map(this::mapToResponse)
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

        card.setFront(request.getFront());
        card.setBack(request.getBack());
        card.setExampleSentence(request.getExampleSentence());
        card.setSourceLanguage(request.getSourceLanguage());
        card.setTargetLanguage(request.getTargetLanguage());
        card.setAudioUrl(request.getAudioUrl());

        if (request.getTagIds() != null) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(request.getTagIds()));
            card.setTags(tags);
        }

        VocabularyCard updatedCard = vocabularyRepository.save(card);
        return mapToResponse(updatedCard);
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

    private VocabularyCardResponse mapToResponse(VocabularyCard card) {
        Set<TagResponse> tagResponses = card.getTags().stream()
                .map(tag -> new TagResponse(tag.getId(), tag.getName(), tag.getColor()))
                .collect(Collectors.toSet());

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
                tagResponses
        );
    }
}
