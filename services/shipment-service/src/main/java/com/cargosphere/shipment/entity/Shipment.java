package com.cargosphere.shipment.entity;

import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import jakarta.persistence.*;
import lombok.*;

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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}