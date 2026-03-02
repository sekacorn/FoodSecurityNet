package com.foodsecuritynet.usersession.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodsecuritynet.usersession.model.UserSession;
import com.foodsecuritynet.usersession.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

    private final UserSessionRepository userSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String SESSION_KEY_PREFIX = "session:";
    private static final long SESSION_TIMEOUT_MINUTES = 60;

    @Transactional
    public UserSession createSession(String userId, Map<String, Object> metadata) {
        log.info("Creating session for user: {}", userId);

        String sessionId = UUID.randomUUID().toString();

        UserSession session = UserSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .status("ACTIVE")
                .metadata(metadata)
                .lastActivityAt(LocalDateTime.now())
                .build();

        UserSession savedSession = userSessionRepository.save(session);

        // Cache in Redis
        String redisKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, savedSession, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        log.info("Session created: {}", sessionId);
        return savedSession;
    }

    public UserSession getSession(String sessionId) {
        log.debug("Fetching session: {}", sessionId);

        // Try Redis first
        String redisKey = SESSION_KEY_PREFIX + sessionId;
        Object cachedSession = redisTemplate.opsForValue().get(redisKey);

        if (cachedSession != null) {
            return objectMapper.convertValue(cachedSession, UserSession.class);
        }

        // Fallback to database
        UserSession session = userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // Update cache
        redisTemplate.opsForValue().set(redisKey, session, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        return session;
    }

    public List<UserSession> getUserSessions(String userId) {
        return userSessionRepository.findByUserId(userId);
    }

    @Transactional
    public UserSession updateSession(String sessionId, Map<String, Object> updates) {
        log.info("Updating session: {}", sessionId);

        UserSession session = userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (updates.containsKey("metadata")) {
            session.setMetadata((Map<String, Object>) updates.get("metadata"));
        }

        if (updates.containsKey("status")) {
            session.setStatus(updates.get("status").toString());
        }

        session.setLastActivityAt(LocalDateTime.now());

        UserSession updatedSession = userSessionRepository.save(session);

        // Update cache
        String redisKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.opsForValue().set(redisKey, updatedSession, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);

        return updatedSession;
    }

    @Transactional
    public void updateHeartbeat(String sessionId) {
        UserSession session = userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        session.setLastActivityAt(LocalDateTime.now());
        userSessionRepository.save(session);

        // Extend cache TTL
        String redisKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.expire(redisKey, SESSION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    @Transactional
    public void endSession(String sessionId) {
        log.info("Ending session: {}", sessionId);

        UserSession session = userSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        session.setStatus("ENDED");
        session.setEndedAt(LocalDateTime.now());
        userSessionRepository.save(session);

        // Remove from cache
        String redisKey = SESSION_KEY_PREFIX + sessionId;
        redisTemplate.delete(redisKey);
    }

    public List<UserSession> getActiveSessions() {
        return userSessionRepository.findByStatus("ACTIVE");
    }

    @Transactional
    public void cleanupInactiveSessions() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(SESSION_TIMEOUT_MINUTES);
        List<UserSession> inactiveSessions = userSessionRepository.findInactiveSessions(cutoffTime);

        for (UserSession session : inactiveSessions) {
            session.setStatus("TIMED_OUT");
            session.setEndedAt(LocalDateTime.now());
            userSessionRepository.save(session);

            // Remove from cache
            String redisKey = SESSION_KEY_PREFIX + session.getSessionId();
            redisTemplate.delete(redisKey);
        }

        log.info("Cleaned up {} inactive sessions", inactiveSessions.size());
    }
}
