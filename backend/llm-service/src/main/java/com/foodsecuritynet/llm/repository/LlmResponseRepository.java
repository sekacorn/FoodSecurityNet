package com.foodsecuritynet.llm.repository;

import com.foodsecuritynet.llm.model.LlmResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LlmResponseRepository extends JpaRepository<LlmResponse, Long> {

    List<LlmResponse> findByUserIdOrderByCreatedAtDesc(String userId);

    List<LlmResponse> findByModel(String model);
}
