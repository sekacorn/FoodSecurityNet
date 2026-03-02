package com.foodsecuritynet.llm.repository;

import com.foodsecuritynet.llm.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    List<ErrorLog> findByUserIdOrderByCreatedAtDesc(String userId);

    List<ErrorLog> findByErrorTypeOrderByCreatedAtDesc(String errorType);

    List<ErrorLog> findByResolved(Boolean resolved);
}
