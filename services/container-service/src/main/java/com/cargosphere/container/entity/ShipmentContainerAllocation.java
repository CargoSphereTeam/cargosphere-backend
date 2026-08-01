package com.cargosphere.container.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "shipment_container_allocations",
        schema = "container_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_shipment_container_type",
                        columnNames = {"shipment_id", "container_type_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentContainerAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "container_type_id", nullable = false)
    private ContainerType containerType;

    @Column(nullable = false)
    private Integer quantity;

    @Builder.Default
    @Column(name = "allocation_status", nullable = false, length = 30)
    private String allocationStatus = "ALLOCATED";

    @Column(length = 255)
    private String notes;

    @Column(name = "allocated_at", nullable = false, updatable = false)
    private LocalDateTime allocatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (allocatedAt == null) {
            allocatedAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }

        if (allocationStatus == null || allocationStatus.isBlank()) {
            allocationStatus = "ALLOCATED";
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}