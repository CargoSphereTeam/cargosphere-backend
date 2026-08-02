package com.cargosphere.shipment.dto.ebill.snapshot;

import java.time.LocalDateTime;

public record EbillDocumentSnapshot(
        Long documentId,
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
