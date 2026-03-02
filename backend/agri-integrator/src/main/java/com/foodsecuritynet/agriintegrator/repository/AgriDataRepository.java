package com.foodsecuritynet.agriintegrator.repository;

import com.foodsecuritynet.agriintegrator.model.AgriData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgriDataRepository extends JpaRepository<AgriData, Long> {

    List<AgriData> findByRegion(String region);

    List<AgriData> findByCropType(String cropType);

    @Query("SELECT DISTINCT a.region FROM AgriData a")
    List<String> findDistinctRegions();

    @Query("SELECT DISTINCT a.cropType FROM AgriData a")
    List<String> findDistinctCropTypes();
}
