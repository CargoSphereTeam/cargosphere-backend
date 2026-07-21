package com.cargosphere.shipment.entity;

import com.cargosphere.shipment.entity.enums.CargoType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "cargo_details",
        schema = "shipment_schema",
        indexes = {
                @Index(name = "idx_cargo_details_shipment_id", columnList = "shipment_id")
        }
)
public class CargoDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "cargo_name", nullable = false, length = 100)
    private String cargoName;

    @Column(name = "cargo_description", length = 255)
    private String cargoDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "cargo_type", length = 50)
    private CargoType cargoType;

    @Column(
            name = "weight_kg",
            nullable = false,
            precision = 13,
            scale = 3
    )
    private BigDecimal weightKg;

    @Column(
            name = "volume_cbm",
            precision = 13,
            scale = 3
    )
    private BigDecimal volumeCbm;

    @Builder.Default
    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Builder.Default
    @Column(name = "is_fragile", nullable = false)
    private Boolean fragile = false;

    @Builder.Default
    @Column(name = "is_hazardous", nullable = false)
    private Boolean hazardous = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.quantity == null) {
            this.quantity = 1;
        }

        if (this.fragile == null) {
            this.fragile = false;
        }

        if (this.hazardous == null) {
            this.hazardous = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}