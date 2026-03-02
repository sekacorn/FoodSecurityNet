package com.foodsecuritynet.agriintegrator.repository;

import com.foodsecuritynet.agriintegrator.model.SocioEconData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocioEconDataRepository extends JpaRepository<SocioEconData, Long> {

    List<SocioEconData> findByRegion(String region);

    @Query("SELECT DISTINCT s.region FROM SocioEconData s")
    List<String> findDistinctRegions();
}
