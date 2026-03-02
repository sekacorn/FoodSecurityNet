package com.foodsecuritynet.usersession.controller;

import com.foodsecuritynet.usersession.model.Annotation;
import com.foodsecuritynet.usersession.service.AnnotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnnotationController {

    private final AnnotationService annotationService;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAnnotation(
            @RequestBody @Valid Map<String, Object> request) {

        log.info("Creating annotation for session: {}", request.get("sessionId"));

        try {
            String sessionId = request.get("sessionId").toString();
            String userId = request.get("userId").toString();
            String content = request.get("content").toString();
            String type = request.getOrDefault("type", "note").toString();
            Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());

            Annotation annotation = annotationService.createAnnotation(sessionId, userId, content, type, metadata);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "status", "success",
                            "message", "Annotation created successfully",
                            "annotationId", annotation.getId(),
                            "annotation", annotation
                    ));

        } catch (Exception e) {
            log.error("Error creating annotation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create annotation: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAnnotation(@PathVariable Long id) {
        log.info("Fetching annotation: {}", id);

        try {
            Annotation annotation = annotationService.getAnnotation(id);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "annotation", annotation
            ));

        } catch (Exception e) {
            log.error("Error fetching annotation", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Annotation not found: " + e.getMessage()));
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionAnnotations(@PathVariable String sessionId) {
        log.info("Fetching annotations for session: {}", sessionId);

        try {
            List<Annotation> annotations = annotationService.getSessionAnnotations(sessionId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "annotations", annotations,
                    "count", annotations.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching session annotations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch annotations: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAnnotations(@PathVariable String userId) {
        log.info("Fetching annotations for user: {}", userId);

        try {
            List<Annotation> annotations = annotationService.getUserAnnotations(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "annotations", annotations,
                    "count", annotations.size()
            ));

        } catch (Exception e) {
            log.error("Error fetching user annotations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch annotations: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAnnotation(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        log.info("Updating annotation: {}", id);

        try {
            Annotation annotation = annotationService.updateAnnotation(id, updates);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Annotation updated successfully",
                    "annotation", annotation
            ));

        } catch (Exception e) {
            log.error("Error updating annotation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update annotation: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAnnotation(@PathVariable Long id) {
        log.info("Deleting annotation: {}", id);

        try {
            annotationService.deleteAnnotation(id);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Annotation deleted successfully"
            ));

        } catch (Exception e) {
            log.error("Error deleting annotation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete annotation: " + e.getMessage()));
        }
    }
}
