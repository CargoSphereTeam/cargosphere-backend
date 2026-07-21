package com.cargosphere.container.dto;

import java.time.LocalDateTime;

public record AllocationResponse(
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