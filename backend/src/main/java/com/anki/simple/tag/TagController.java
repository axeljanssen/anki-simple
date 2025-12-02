package com.anki.simple.tag;

import com.anki.simple.tag.dto.TagRequest;
import com.anki.simple.tag.dto.TagResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TagResponse response = tagService.createTag(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TagResponse response = tagService.updateTag(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TagResponse> tags = tagService.getAllTags(userDetails.getUsername());
        return ResponseEntity.ok(tags);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        tagService.deleteTag(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
