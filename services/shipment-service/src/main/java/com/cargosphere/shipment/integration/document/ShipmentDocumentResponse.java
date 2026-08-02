package com.cargosphere.shipment.integration.document;

import java.time.LocalDateTime;

public record ShipmentDocumentResponse(
        Long id,
        Long shipmentId,
        String documentType,
        Boolean required,
        String verificationStatus,
        Long verifiedBy,
        LocalDateTime verifiedAt,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
