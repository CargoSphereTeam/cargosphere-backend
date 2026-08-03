package com.cargosphere.shipment.entity;

import com.cargosphere.shipment.entity.enums.CargoType;
import com.cargosphere.shipment.entity.enums.CargoVerificationStatus;
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
        name = "cargo_verifications",
        schema = "shipment_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cargo_verifications_cargo_detail",
                        columnNames = "cargo_detail_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_cargo_verifications_status",
                        columnList = "verification_status"
                )
        }
)
public class CargoVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cargo_detail_id",
            nullable = false,
            unique = true
    )
    private CargoDetail cargoDetail;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "verification_status",
            nullable = false,
            length = 20
    )
    private CargoVerificationStatus verificationStatus =
            CargoVerificationStatus.DRAFT;

    @Column(
            name = "confirmed_cargo_name",
            length = 100
    )
    private String confirmedCargoName;

    @Column(
            name = "confirmed_cargo_description",
            length = 255
    )
    private String confirmedCargoDescription;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "confirmed_cargo_type",
            length = 50
    )
    private CargoType confirmedCargoType;

    @Column(
            name = "confirmed_weight_kg",
            precision = 13,
            scale = 3
    )
    private BigDecimal confirmedWeightKg;

    @Column(
            name = "confirmed_volume_cbm",
            precision = 13,
            scale = 3
    )
    private BigDecimal confirmedVolumeCbm;

    @Column(name = "confirmed_quantity")
    private Integer confirmedQuantity;

    @Column(name = "confirmed_is_fragile")
    private Boolean confirmedFragile;

    @Column(name = "confirmed_is_hazardous")
    private Boolean confirmedHazardous;

    @Column(
            name = "verification_remarks",
            length = 500
    )
    private String verificationRemarks;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (this.verificationStatus == null) {
            this.verificationStatus =
                    CargoVerificationStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}