package com.cargosphere.shipment.integration.container;

import java.time.LocalDateTime;

public record ContainerAllocationResponse(
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
