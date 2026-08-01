package com.cargosphere.shipment.dto;

import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "UpdateShipmentStatusRequest",
        description = "Request body used to update shipment status"
)
public class UpdateShipmentStatusRequest {

    @NotNull(message = "Shipment status is required")
    @Schema(
            description = "New shipment lifecycle status",
            example = "IN_TRANSIT",
            allowableValues = {
                    "CREATED",
                    "BOOKED",
                    "IN_TRANSIT",
                    "DELIVERED",
                    "CANCELLED"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ShipmentStatus status;
}
