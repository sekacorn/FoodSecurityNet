package com.foodsecuritynet.usersession.service;

import com.foodsecuritynet.usersession.model.Annotation;
import com.foodsecuritynet.usersession.repository.AnnotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnotationService {

    private final AnnotationRepository annotationRepository;

    @Transactional
    public Annotation createAnnotation(String sessionId, String userId, String content, String type, Map<String, Object> metadata) {
        log.info("Creating annotation for session: {}", sessionId);

        Annotation annotation = Annotation.builder()
                .sessionId(sessionId)
                .userId(userId)
                .content(content)
                .type(type)
                .metadata(metadata)
                .build();

        return annotationRepository.save(annotation);
    }

    public Annotation getAnnotation(Long id) {
        return annotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotation not found: " + id));
    }

    public List<Annotation> getSessionAnnotations(String sessionId) {
        return annotationRepository.findBySessionId(sessionId);
    }

    public List<Annotation> getUserAnnotations(String userId) {
        return annotationRepository.findByUserId(userId);
    }

    @Transactional
    public Annotation updateAnnotation(Long id, Map<String, Object> updates) {
        log.info("Updating annotation: {}", id);

        Annotation annotation = annotationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Annotation not found: " + id));

        if (updates.containsKey("content")) {
            annotation.setContent(updates.get("content").toString());
        }

        if (updates.containsKey("type")) {
            annotation.setType(updates.get("type").toString());
        }

        if (updates.containsKey("metadata")) {
            annotation.setMetadata((Map<String, Object>) updates.get("metadata"));
        }

        return annotationRepository.save(annotation);
    }

    @Transactional
    public void deleteAnnotation(Long id) {
        annotationRepository.deleteById(id);
        log.info("Deleted annotation: {}", id);
    }
}
