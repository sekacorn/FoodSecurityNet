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
@Table(name = "agri_maps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AgriMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String region;

    @NotNull
    @Column(name = "map_type", nullable = false)
    private String mapType;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "layers", columnDefinition = "jsonb")
    private Map<String, Object> layers;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bounds", columnDefinition = "jsonb")
    private Map<String, Object> bounds;

    private Integer zoom;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "center", columnDefinition = "jsonb")
    private Map<String, Object> center;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
