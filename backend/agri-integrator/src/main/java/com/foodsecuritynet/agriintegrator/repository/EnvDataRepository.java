package com.foodsecuritynet.agriintegrator.repository;

import com.foodsecuritynet.agriintegrator.model.EnvData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvDataRepository extends JpaRepository<EnvData, Long> {

    List<EnvData> findByRegion(String region);

    @Query("SELECT DISTINCT e.region FROM EnvData e")
    List<String> findDistinctRegions();
}
