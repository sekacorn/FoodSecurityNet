package com.foodsecuritynet.agrivisualizer.repository;

import com.foodsecuritynet.agrivisualizer.model.Visualization;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VisualizationRepository extends JpaRepository<Visualization, Long> {

    List<Visualization> findByType(String type, Pageable pageable);

    long countByType(String type);

    List<Visualization> findByStatus(String status);

    @Query("SELECT v.type as type, COUNT(v) as count FROM Visualization v GROUP BY v.type")
    List<Map<String, Object>> countByTypeGrouped();
}
