package com.cargosphere.container.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "container_types", schema = "container_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "container_type_id")
    private Long containerTypeId;

    @Column(name = "type_code", nullable = false, unique = true, length = 30)
    private String typeCode;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(length = 255)
    private String description;

    @Column(name = "max_weight_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxWeightKg;

    @Column(name = "max_volume_cbm", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxVolumeCbm;

    @Column(name = "length_m", precision = 8, scale = 2)
    private BigDecimal lengthM;

    @Column(name = "width_m", precision = 8, scale = 2)
    private BigDecimal widthM;

    @Column(name = "height_m", precision = 8, scale = 2)
    private BigDecimal heightM;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (active == null) {
            active = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}