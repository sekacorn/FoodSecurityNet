package com.foodsecuritynet.collaboration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsecuritynet.collaboration.model.CollabSession;
import com.foodsecuritynet.collaboration.model.UserAction;
import com.foodsecuritynet.collaboration.repository.CollabSessionRepository;
import com.foodsecuritynet.collaboration.repository.UserActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationSessionService {

    private final CollabSessionRepository collabSessionRepository;
    private final UserActionRepository userActionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SESSION_USERS_KEY_PREFIX = "session:users:";
    private static final long SESSION_TIMEOUT_MINUTES = 120;

    @Transactional
    public CollabSession createSession(String sessionName, String creatorId) {
        log.info("Creating collaboration session: {}", sessionName);

        String sessionId = UUID.randomUUID().toString();

        CollabSession session = CollabSession.builder()
                .sessionId(sessionId)
                .sessionName(sessionName)
                .creatorId(creatorId)
                .status("ACTIVE")
                .metadata(new HashMap<>())
                .build();

        CollabSession savedSession = collabSessionRepository.save(session);

        // Initialize session in Redis
        String redisKey = SESSION_USERS_KEY_PREFIX + sessionId;
        redisTemplate.opsForSet().add(redisKey, creatorId);
        redisTemplate.expire(redisKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        log.info("Session created: {}", sessionId);
        return savedSession;
    }

    public CollabSession getSession(String sessionId) {
        return collabSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }

    public void addUserToSession(String sessionId, String userId, String userName) {
        log.info("Adding user {} to session {}", userId, sessionId);

        String redisKey = SESSION_USERS_KEY_PREFIX + sessionId;
        redisTemplate.opsForSet().add(redisKey, userId);
        redisTemplate.expire(redisKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // Log join action
        UserAction joinAction = UserAction.builder()
                .sessionId(sessionId)
                .userId(userId)
                .actionType("JOIN")
                .actionData(Map.of("userName", userName))
                .build();

        userActionRepository.save(joinAction);
    }

    public void removeUserFromSession(String sessionId, String userId) {
        log.info("Removing user {} from session {}", userId, sessionId);

        String redisKey = SESSION_USERS_KEY_PREFIX + sessionId;
        redisTemplate.opsForSet().remove(redisKey, userId);

        // Log leave action
        UserAction leaveAction = UserAction.builder()
                .sessionId(sessionId)
                .userId(userId)
                .actionType("LEAVE")
                .actionData(Map.of())
                .build();

        userActionRepository.save(leaveAction);
    }

    public List<String> getActiveUsers(String sessionId) {
        String redisKey = SESSION_USERS_KEY_PREFIX + sessionId;
        Set<Object> users = redisTemplate.opsForSet().members(redisKey);

        if (users == null) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(Object::toString)
                .toList();
    }

    @Transactional
    public void saveUserAction(UserAction userAction) {
        userActionRepository.save(userAction);
    }

    public List<UserAction> getSessionHistory(String sessionId) {
        return userActionRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void endSession(String sessionId) {
        log.info("Ending session: {}", sessionId);

        CollabSession session = getSession(sessionId);
        session.setStatus("ENDED");
        session.setEndedAt(LocalDateTime.now());
        collabSessionRepository.save(session);

        // Clear Redis data
        String redisKey = SESSION_USERS_KEY_PREFIX + sessionId;
        redisTemplate.delete(redisKey);
    }

    public List<CollabSession> getActiveSessionsByCreator(String creatorId) {
        return collabSessionRepository.findByCreatorIdAndStatus(creatorId, "ACTIVE");
    }
}
