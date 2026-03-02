package com.foodsecuritynet.usersession.controller;

import com.foodsecuritynet.usersession.model.UserSession;
import com.foodsecuritynet.usersession.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SessionController {

    private final SessionService sessionService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestBody @Valid Map<String, Object> request) {

        log.info("Creating session for user: {}", request.get("userId"));

        try {
            String userId = request.get("userId").toString();
            Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());

            UserSession session = sessionService.createSession(userId, metadata);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "success",
                            "message", "Session created successfully",
                            "sessionId", session.getSessionId(),
                            "session", session
                    ));

        } catch (Exception e) {
            log.error("Error creating session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create session: " + e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        log.info("Fetching session: {}", sessionId);

        try {
            UserSession session = sessionService.getSession(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "session", session
            ));

        } catch (Exception e) {
            log.error("Error fetching session", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Session not found: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable String userId) {
        log.info("Fetching sessions for user: {}", userId);

        try {
            List<UserSession> sessions = sessionService.getUserSessions(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "sessions", sessions,
                    "count", sessions.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching user sessions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch sessions: " + e.getMessage()));
        }
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> updates) {

        log.info("Updating session: {}", sessionId);

        try {
            UserSession session = sessionService.updateSession(sessionId, updates);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Session updated successfully",
                    "session", session
            ));

        } catch (Exception e) {
            log.error("Error updating session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update session: " + e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/heartbeat")
    public ResponseEntity<Map<String, Object>> sendHeartbeat(@PathVariable String sessionId) {
        log.debug("Heartbeat received for session: {}", sessionId);

        try {
            sessionService.updateHeartbeat(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Heartbeat recorded"
            ));

        } catch (Exception e) {
            log.error("Error processing heartbeat", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process heartbeat: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, String>> endSession(@PathVariable String sessionId) {
        log.info("Ending session: {}", sessionId);

        try {
            sessionService.endSession(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Session ended successfully"
            ));

        } catch (Exception e) {
            log.error("Error ending session", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to end session: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveSessions() {
        log.info("Fetching active sessions");

        try {
            List<UserSession> activeSessions = sessionService.getActiveSessions();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "sessions", activeSessions,
                    "count", activeSessions.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching active sessions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch active sessions: " + e.getMessage()));
        }
    }
}
