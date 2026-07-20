package com.cargosphere.shipment.entity;

import com.cargosphere.shipment.entity.enums.ShipmentEventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "shipment_events",
        schema = "shipment_schema",
        indexes = {
                @Index(name = "idx_shipment_events_shipment_id", columnList = "shipment_id"),
                @Index(name = "idx_shipment_events_event_type", columnList = "event_type")
        }
)
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private ShipmentEventType eventType;

    @Column(name = "event_description", length = 255)
    private String eventDescription;

    @Column(name = "event_location", length = 150)
    private String eventLocation;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (this.eventTime == null) {
            this.eventTime = now;
        }

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}