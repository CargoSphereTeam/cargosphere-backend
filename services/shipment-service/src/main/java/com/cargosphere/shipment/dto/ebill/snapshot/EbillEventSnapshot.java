package com.cargosphere.shipment.dto.ebill.snapshot;

import com.cargosphere.shipment.entity.enums.ShipmentEventType;

import java.time.LocalDateTime;

public record EbillEventSnapshot(
        Long eventId,
        Long shipmentId,
        ShipmentEventType eventType,
        String eventDescription,
        String eventLocation,
        LocalDateTime eventTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
