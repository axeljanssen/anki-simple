package com.anki.simple.vocabulary;

import com.anki.simple.vocabulary.dto.VocabularyCardLeanResponse;
import com.anki.simple.vocabulary.dto.VocabularyCardRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @PostMapping
    public ResponseEntity<VocabularyCardResponse> createCard(
            @Valid @RequestBody VocabularyCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VocabularyCardResponse response = vocabularyService.createCard(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VocabularyCardLeanResponse>> getAllCards(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) String searchTerm,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VocabularyCardLeanResponse> cards = vocabularyService.getAllCards(
                userDetails.getUsername(), sortBy, sortDirection, searchTerm);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/due")
    public ResponseEntity<List<VocabularyCardResponse>> getDueCards(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<VocabularyCardResponse> cards = vocabularyService.getDueCards(userDetails.getUsername());
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/due/count")
    public ResponseEntity<Long> getDueCardsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = vocabularyService.getDueCardsCount(userDetails.getUsername());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCardsCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        long count = vocabularyService.getTotalCount(userDetails.getUsername());
        return ResponseEntity.ok(count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VocabularyCardResponse> getCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        VocabularyCardResponse response = vocabularyService.getCard(id, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VocabularyCardResponse> updateCard(
            @PathVariable Long id,
            @Valid @RequestBody VocabularyCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VocabularyCardResponse response = vocabularyService.updateCard(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        vocabularyService.deleteCard(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
