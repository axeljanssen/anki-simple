package com.anki.simple.review;

import com.anki.simple.vocabulary.VocabularyCard;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_history")
@Data
@NoArgsConstructor
public class ReviewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private VocabularyCard card;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(nullable = false)
    private Integer quality;

    @Column(name = "ease_factor")
    private Double easeFactor;

    @Column(name = "interval_days")
    private Integer intervalDays;

    @PrePersist
    protected void onCreate() {
        reviewedAt = LocalDateTime.now();
    }
}
