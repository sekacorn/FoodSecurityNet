package com.foodsecuritynet.usersession.repository;

import com.foodsecuritynet.usersession.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionId(String sessionId);

    List<UserSession> findByUserId(String userId);

    List<UserSession> findByStatus(String status);

    @Query("SELECT s FROM UserSession s WHERE s.status = 'ACTIVE' AND s.lastActivityAt < :cutoffTime")
    List<UserSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}
