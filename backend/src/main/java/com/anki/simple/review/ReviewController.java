package com.anki.simple.review;

import com.anki.simple.review.dto.ReviewRequest;
import com.anki.simple.vocabulary.dto.VocabularyCardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<VocabularyCardResponse> reviewCard(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        VocabularyCardResponse response = reviewService.reviewCard(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }
}
