package com.cargosphere.shipment.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "CargoVerificationRequest",
        description = "Request body for saving or confirming shipment cargo verification"
)
public class CargoVerificationRequest {

    @NotNull(message = "Cargo verification action is required")
    @Schema(
            description = "Determines whether verification is saved as a draft or confirmed",
            example = "SAVE_DRAFT",
            allowableValues = {
                    "SAVE_DRAFT",
                    "CONFIRM_AND_CONTINUE"
            },
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private CargoVerificationAction action;

    @NotEmpty(message = "At least one cargo verification item is required")
    @Valid
    @Schema(
            description = "Cargo verification values for shipment cargo items",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<CargoVerificationItemRequest> items;
}