package com.foodsecuritynet.collaboration.repository;

import com.foodsecuritynet.collaboration.model.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    List<UserAction> findByUserId(String userId);

    List<UserAction> findByActionType(String actionType);
}
