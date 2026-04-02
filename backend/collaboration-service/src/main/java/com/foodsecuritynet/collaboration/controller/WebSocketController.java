package com.foodsecuritynet.collaboration.controller;

import com.foodsecuritynet.collaboration.model.UserAction;
import com.foodsecuritynet.collaboration.service.CollaborationSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final CollaborationSessionService collaborationSessionService;

    @MessageMapping("/session/{sessionId}/join")
    @SendTo("/topic/session/{sessionId}")
    public Map<String, Object> joinSession(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {

        log.info("User joining session: {}", sessionId);

        String userId = payload.get("userId").toString();
        String userName = payload.getOrDefault("userName", "Anonymous").toString();

        collaborationSessionService.addUserToSession(sessionId, userId, userName);

        return Map.of(
                "type", "USER_JOINED",
                "sessionId", sessionId,
                "userId", userId,
                "userName", userName,
                "timestamp", System.currentTimeMillis()
        );
    }

    @MessageMapping("/session/{sessionId}/leave")
    @SendTo("/topic/session/{sessionId}")
    public Map<String, Object> leaveSession(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {

        log.info("User leaving session: {}", sessionId);

        String userId = payload.get("userId").toString();

        collaborationSessionService.removeUserFromSession(sessionId, userId);

        return Map.of(
                "type", "USER_LEFT",
                "sessionId", sessionId,
                "userId", userId,
                "timestamp", System.currentTimeMillis()
        );
    }

    @MessageMapping("/session/{sessionId}/action")
    public void broadcastAction(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {

        log.debug("Broadcasting action to session: {}", sessionId);

        String userId = payload.get("userId").toString();
        String actionType = payload.get("actionType").toString();
        Map<String, Object> actionData = (Map<String, Object>) payload.getOrDefault("data", Map.of());

        UserAction userAction = UserAction.builder()
                .sessionId(sessionId)
                .userId(userId)
                .actionType(actionType)
                .actionData(actionData)
                .build();

        collaborationSessionService.saveUserAction(userAction);

        Map<String, Object> message = Map.of(
                "type", "USER_ACTION",
                "sessionId", sessionId,
                "userId", userId,
                "actionType", actionType,
                "data", actionData,
                "timestamp", System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    @MessageMapping("/session/{sessionId}/cursor")
    public void broadcastCursor(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {

        log.trace("Broadcasting cursor position for session: {}", sessionId);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId + "/cursors", payload);
    }

    @MessageMapping("/session/{sessionId}/annotation")
    public void broadcastAnnotation(
            @DestinationVariable String sessionId,
            @Payload Map<String, Object> payload) {

        log.debug("Broadcasting annotation to session: {}", sessionId);

        String userId = payload.get("userId").toString();
        String content = payload.get("content").toString();

        Map<String, Object> message = Map.of(
                "type", "ANNOTATION",
                "sessionId", sessionId,
                "userId", userId,
                "content", content,
                "timestamp", System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, message);
    }

    @SubscribeMapping("/session/{sessionId}")
    public Map<String, Object> onSubscribe(@DestinationVariable String sessionId) {
        log.info("Client subscribed to session: {}", sessionId);

        return Map.of(
                "type", "SUBSCRIPTION_CONFIRMED",
                "sessionId", sessionId,
                "activeUsers", collaborationSessionService.getActiveUsers(sessionId),
                "timestamp", System.currentTimeMillis()
        );
    }

    @RestController
    @RequestMapping("/api/v1/collaboration")
    @RequiredArgsConstructor
    public static class RestApiController {

        private final CollaborationSessionService collaborationSessionService;

        @PostMapping("/sessions/create")
        public ResponseEntity<Map<String, Object>> createSession(@RequestBody Map<String, Object> request) {
            String sessionName = request.getOrDefault("sessionName", "Untitled Session").toString();
            String creatorId = request.get("creatorId").toString();

            var session = collaborationSessionService.createSession(sessionName, creatorId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "session", session
            ));
        }

        @GetMapping("/sessions")
        public ResponseEntity<Map<String, Object>> listSessions(
                @RequestParam(required = false) String creatorId
        ) {
            var sessions = collaborationSessionService.getActiveSessions(creatorId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "sessions", sessions,
                    "count", sessions.size()
            ));
        }

        @GetMapping("/sessions/{sessionId}")
        public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
            var session = collaborationSessionService.getSession(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "session", session
            ));
        }

        @GetMapping("/sessions/{sessionId}/users")
        public ResponseEntity<Map<String, Object>> getActiveUsers(@PathVariable String sessionId) {
            var users = collaborationSessionService.getActiveUsers(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "users", users,
                    "count", users.size()
            ));
        }

        @GetMapping("/sessions/{sessionId}/history")
        public ResponseEntity<Map<String, Object>> getSessionHistory(@PathVariable String sessionId) {
            var history = collaborationSessionService.getSessionHistory(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "history", history,
                    "count", history.size()
            ));
        }
    }
}
