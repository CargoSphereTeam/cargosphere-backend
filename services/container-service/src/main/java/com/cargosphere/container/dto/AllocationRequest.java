package com.cargosphere.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "AllocationRequest",
        description =
                "Request body used to create or update a container allocation"
)
public record AllocationRequest(

        @NotNull(message = "Shipment ID is required")
        @Schema(
                description = "Shipment database ID",
                example = "101",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long shipmentId,

        @NotNull(message = "Container type ID is required")
        @Schema(
                description = "Container type database ID",
                example = "1",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Long containerTypeId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Schema(
                description = "Number of containers allocated",
                example = "2",
                minimum = "1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer quantity,

        @Size(
                max = 30,
                message =
                        "Allocation status cannot exceed 30 characters"
        )
        @Schema(
                description = "Allocation lifecycle status",
                example = "ALLOCATED",
                maxLength = 30
        )
        String allocationStatus,

        @Size(max = 255, message = "Notes cannot exceed 255 characters")
        @Schema(
                description = "Optional allocation notes",
                example = "Containers reserved for road shipment",
                maxLength = 255
        )
        String notes
) {
}
