package com.foodsecuritynet.usersession.repository;

import com.foodsecuritynet.usersession.model.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

    List<Annotation> findBySessionId(String sessionId);

    List<Annotation> findByUserId(String userId);

    List<Annotation> findByType(String type);
}
