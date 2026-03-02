package com.foodsecuritynet.collaboration.repository;

import com.foodsecuritynet.collaboration.model.CollabSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollabSessionRepository extends JpaRepository<CollabSession, Long> {

    Optional<CollabSession> findBySessionId(String sessionId);

    List<CollabSession> findByCreatorId(String creatorId);

    List<CollabSession> findByCreatorIdAndStatus(String creatorId, String status);

    List<CollabSession> findByStatus(String status);
}
