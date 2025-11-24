package com.anki.simple.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Long cardId;

    @NotNull
    @Min(0)
    @Max(5)
    private Integer quality;
}
