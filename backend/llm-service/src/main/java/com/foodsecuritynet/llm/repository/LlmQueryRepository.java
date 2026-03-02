package com.foodsecuritynet.llm.repository;

import com.foodsecuritynet.llm.model.LlmQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for LlmQuery entities
 */
@Repository
public interface LlmQueryRepository extends JpaRepository<LlmQuery, Long> {

    /**
     * Find queries by user ID
     */
    List<LlmQuery> findByUserId(Long userId);

    /**
     * Find queries by user ID and status
     */
    List<LlmQuery> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find queries by query type
     */
    List<LlmQuery> findByQueryType(String queryType);

    /**
     * Find recent queries by user
     */
    List<LlmQuery> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find queries by date range
     */
    @Query("SELECT q FROM LlmQuery q WHERE q.createdAt BETWEEN :startDate AND :endDate")
    List<LlmQuery> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Count queries by user
     */
    Long countByUserId(Long userId);

    /**
     * Find failed queries
     */
    List<LlmQuery> findByStatus(String status);

    /**
     * Calculate average response time
     */
    @Query("SELECT AVG(q.responseTimeMs) FROM LlmQuery q WHERE q.status = 'COMPLETED'")
    Double getAverageResponseTime();

    /**
     * Calculate total tokens used by user
     */
    @Query("SELECT SUM(q.tokensUsed) FROM LlmQuery q WHERE q.userId = :userId")
    Long getTotalTokensUsedByUser(@Param("userId") Long userId);
}
