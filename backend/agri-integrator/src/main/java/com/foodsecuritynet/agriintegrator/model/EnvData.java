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
@Table(name = "env_data")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EnvData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String region;

    @NotNull
    @Column(name = "measurement_date", nullable = false)
    private LocalDateTime measurementDate;

    @Column(name = "temperature_avg")
    private Double temperatureAvg;

    @Column(name = "temperature_min")
    private Double temperatureMin;

    @Column(name = "temperature_max")
    private Double temperatureMax;

    @Column(name = "temperature_unit")
    private String temperatureUnit;

    private Double rainfall;

    @Column(name = "rainfall_unit")
    private String rainfallUnit;

    private Double humidity;

    @Column(name = "humidity_unit")
    private String humidityUnit;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_speed_unit")
    private String windSpeedUnit;

    @Column(name = "soil_moisture")
    private Double soilMoisture;

    @Column(name = "soil_moisture_unit")
    private String soilMoistureUnit;

    @Column(name = "soil_ph")
    private Double soilPh;

    @Column(name = "solar_radiation")
    private Double solarRadiation;

    @Column(name = "solar_radiation_unit")
    private String solarRadiationUnit;

    private Double latitude;

    private Double longitude;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "air_quality_index")
    private Integer airQualityIndex;

    @Column(length = 1000)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
