package com.cargosphere.shipment.entity;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "shipments",
        schema = "shipment_schema",
        indexes = {
                @Index(
                        name = "idx_shipments_client_user_id",
                        columnList = "client_user_id"
                ),
                @Index(
                        name = "idx_shipments_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_shipments_shipment_number",
                        columnList = "shipment_number"
                ),
                @Index(
                        name = "idx_shipments_processing_stage",
                        columnList = "processing_stage"
                )
        }
)
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "shipment_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String shipmentNumber;

    @Column(name = "client_user_id", nullable = false)
    private Long clientUserId;

    @Column(
            name = "origin_location",
            nullable = false,
            length = 150
    )
    private String originLocation;

    @Column(
            name = "destination_location",
            nullable = false,
            length = 150
    )
    private String destinationLocation;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "shipment_type",
            nullable = false,
            length = 50
    )
    private ShipmentType shipmentType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private ShipmentStatus status = ShipmentStatus.CREATED;

    @Column(name = "expected_pickup_date")
    private LocalDate expectedPickupDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(
            name = "processing_stage",
            nullable = false,
            length = 40
    )
    private ProcessingStage processingStage =
            ProcessingStage.PENDING_ADMIN_REVIEW;

    @Column(name = "processing_started_at")
    private OffsetDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private OffsetDateTime processingCompletedAt;

    @Column(
            name = "ebill_number",
            unique = true,
            length = 50
    )
    private String ebillNumber;

    @Column(name = "ebill_version")
    private Integer ebillVersion;

    @Column(name = "ebill_generated_at")
    private OffsetDateTime ebillGeneratedAt;

    @Column(name = "ebill_generated_by")
    private Long ebillGeneratedBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "ebill_snapshot",
            columnDefinition = "jsonb"
    )
    private String ebillSnapshot;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private OffsetDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        this.createdAt = now;
        this.updatedAt = now;

        if (this.status == null) {
            this.status = ShipmentStatus.CREATED;
        }

        if (this.processingStage == null) {
            this.processingStage =
                    ProcessingStage.PENDING_ADMIN_REVIEW;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}