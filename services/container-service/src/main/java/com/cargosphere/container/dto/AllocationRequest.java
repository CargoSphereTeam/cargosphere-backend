package com.cargosphere.container.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AllocationRequest(

        @NotNull(message = "Shipment ID is required")
        Long shipmentId,

        @NotNull(message = "Container type ID is required")
        Long containerTypeId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Size(max = 30, message = "Allocation status cannot exceed 30 characters")
        String allocationStatus,

        @Size(max = 255, message = "Notes cannot exceed 255 characters")
        String notes
) {
}