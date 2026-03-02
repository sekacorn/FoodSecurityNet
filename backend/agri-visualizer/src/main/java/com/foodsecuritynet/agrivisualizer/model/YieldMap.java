package com.foodsecuritynet.agrivisualizer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "yield_maps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class YieldMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String region;

    @NotNull
    @Column(name = "crop_type", nullable = false)
    private String cropType;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    @NotNull
    @Column(nullable = false)
    private String season;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "yield_data", columnDefinition = "jsonb")
    private Map<String, Object> yieldData;

    @Column(name = "avg_yield")
    private Double avgYield;

    @Column(name = "min_yield")
    private Double minYield;

    @Column(name = "max_yield")
    private Double maxYield;

    @Column(name = "yield_unit")
    private String yieldUnit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "spatial_data", columnDefinition = "jsonb")
    private Map<String, Object> spatialData;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
