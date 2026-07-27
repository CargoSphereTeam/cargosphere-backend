package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.ShipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CreateShipmentRequest",
        description = "Request body used to create a new shipment"
)
public class CreateShipmentRequest {

    @NotNull(message = "Client user id is required")
    @Positive(message = "Client user id must be greater than zero")
    @Schema(
            description = "Client user ID that owns the shipment",
            example = "25",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long clientUserId;

    @NotBlank(message = "Origin location is required")
    @Size(
            max = 150,
            message = "Origin location must not exceed 150 characters"
    )
    @Schema(
            description = "Location from which the shipment originates",
            example = "Mumbai",
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String originLocation;

    @NotBlank(message = "Destination location is required")
    @Size(
            max = 150,
            message = "Destination location must not exceed 150 characters"
    )
    @Schema(
            description = "Final destination of the shipment",
            example = "Pune",
            maxLength = 150,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String destinationLocation;

    @NotNull(message = "Shipment type is required")
    @Schema(
            description = "Transportation mode used for the shipment",
            example = "ROAD",
            allowableValues = {
                    "ROAD",
                    "RAIL",
                    "SEA",
                    "AIR"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ShipmentType shipmentType;

    @FutureOrPresent(
            message = "Expected pickup date cannot be in the past"
    )
    @Schema(
            description = "Planned pickup date in ISO format",
            example = "2026-08-01",
            type = "string",
            format = "date"
    )
    private LocalDate expectedPickupDate;

    @FutureOrPresent(
            message = "Expected delivery date cannot be in the past"
    )
    @Schema(
            description = "Planned delivery date in ISO format",
            example = "2026-08-05",
            type = "string",
            format = "date"
    )
    private LocalDate expectedDeliveryDate;
}
