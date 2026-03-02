package com.foodsecuritynet.agriintegrator.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "agri_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AgriData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String region;

    @NotNull
    @Column(nullable = false)
    private String cropType;

    @Column(name = "crop_variety")
    private String cropVariety;

    @NotNull
    @Column(nullable = false)
    private Double yield;

    @Column(name = "yield_unit")
    private String yieldUnit;

    @Column(name = "planted_area")
    private Double plantedArea;

    @Column(name = "area_unit")
    private String areaUnit;

    @Column(name = "harvest_date")
    private LocalDateTime harvestDate;

    @Column(name = "planting_date")
    private LocalDateTime plantingDate;

    @Column(name = "irrigation_type")
    private String irrigationType;

    @Column(name = "fertilizer_used")
    private String fertilizerUsed;

    @Column(name = "pesticide_used")
    private String pesticideUsed;

    private Double latitude;

    private Double longitude;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "farming_method")
    private String farmingMethod;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
