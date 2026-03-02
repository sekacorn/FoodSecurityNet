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
@Table(name = "socio_econ_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SocioEconData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String region;

    @NotNull
    @Column(name = "data_date", nullable = false)
    private LocalDateTime dataDate;

    private Long population;

    @Column(name = "farming_households")
    private Integer farmingHouseholds;

    @Column(name = "avg_household_income")
    private Double avgHouseholdIncome;

    @Column(name = "income_currency")
    private String incomeCurrency;

    @Column(name = "poverty_rate")
    private Double povertyRate;

    @Column(name = "unemployment_rate")
    private Double unemploymentRate;

    @Column(name = "literacy_rate")
    private Double literacyRate;

    @Column(name = "market_access")
    private String marketAccess;

    @Column(name = "avg_land_size")
    private Double avgLandSize;

    @Column(name = "land_size_unit")
    private String landSizeUnit;

    @Column(name = "food_security_index")
    private Double foodSecurityIndex;

    @Column(name = "malnutrition_rate")
    private Double malnutritionRate;

    @Column(name = "agricultural_employment_rate")
    private Double agriculturalEmploymentRate;

    @Column(name = "infrastructure_score")
    private Integer infrastructureScore;

    @Column(name = "technology_adoption_rate")
    private Double technologyAdoptionRate;

    private Double latitude;

    private Double longitude;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
