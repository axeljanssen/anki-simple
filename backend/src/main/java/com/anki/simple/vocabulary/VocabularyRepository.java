package com.anki.simple.vocabulary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<VocabularyCard, Long> {
    List<VocabularyCard> findByUserId(Long userId);

    @Query("SELECT v FROM VocabularyCard v WHERE v.user.id = :userId AND v.nextReview <= :now ORDER BY v.nextReview ASC")
    List<VocabularyCard> findDueCards(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT v FROM VocabularyCard v JOIN v.tags t WHERE v.user.id = :userId AND t.id = :tagId")
    List<VocabularyCard> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    long countByUserIdAndNextReviewBefore(Long userId, LocalDateTime now);
}
