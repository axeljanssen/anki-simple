package com.anki.simple.vocabulary;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VocabularyRepository extends JpaRepository<VocabularyCard, Long> {
    List<VocabularyCard> findByUserId(Long userId);

    List<VocabularyCard> findByUserId(Long userId, Sort sort);

    @Query("SELECT v FROM VocabularyCard v WHERE v.user.id = :userId AND v.nextReview <= :now ORDER BY v.nextReview ASC")
    List<VocabularyCard> findDueCards(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT v FROM VocabularyCard v JOIN v.tags t WHERE v.user.id = :userId AND t.id = :tagId")
    List<VocabularyCard> findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    long countByUserIdAndNextReviewBefore(Long userId, LocalDateTime now);

    @Query("SELECT v FROM VocabularyCard v WHERE v.user.id = :userId " +
           "AND (LOWER(v.front) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(v.back) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(v.exampleSentence) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<VocabularyCard> searchCards(@Param("userId") Long userId,
                                      @Param("searchTerm") String searchTerm,
                                      Sort sort);
}
