package com.cargosphere.shipment.dto.ebill.snapshot;

import java.time.LocalDateTime;

public record EbillContainerAllocationSnapshot(
        Long allocationId,
        Long shipmentId,
        Long containerTypeId,
        String containerTypeCode,
        String containerTypeName,
        Integer quantity,
        String allocationStatus,
        String notes,
        LocalDateTime allocatedAt,
        LocalDateTime updatedAt
) {
}
