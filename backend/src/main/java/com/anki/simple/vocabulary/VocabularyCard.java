package com.anki.simple.vocabulary;

import com.anki.simple.user.User;
import com.anki.simple.tag.Tag;
import com.anki.simple.review.ReviewHistory;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.repository.cdi.Eager;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "vocabulary_cards")
@Data
@NoArgsConstructor
public class VocabularyCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String front;

    @Column(nullable = false)
    private String back;

    @Column(length = 1000)
    private String exampleSentence;

    @Enumerated(EnumType.STRING)
    @Column(name = "language_selection", length = 20)
    private LanguageSelection languageSelection;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @Column(name = "next_review")
    private LocalDateTime nextReview;

    @Column(name = "ease_factor")
    private Double easeFactor = 2.5;

    @Column(name = "interval_days")
    private Integer intervalDays = 0;

    @Column(name = "repetitions")
    private Integer repetitions = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "card_tags",
        joinColumns = @JoinColumn(name = "card_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "card", cascade = {CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.MERGE}, orphanRemoval = true)
    private List<ReviewHistory> reviewHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        nextReview = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        VocabularyCard that = (VocabularyCard) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
