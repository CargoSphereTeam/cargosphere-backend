package com.cargosphere.shipment.dto.ebill.snapshot;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EbillShipmentSnapshot(
        Long shipmentId,
        String shipmentNumber,
        Long clientUserId,
        String originLocation,
        String destinationLocation,
        ShipmentType shipmentType,
        ShipmentStatus status,
        LocalDate expectedPickupDate,
        LocalDate expectedDeliveryDate,
        ProcessingStage processingStage,
        OffsetDateTime processingStartedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
