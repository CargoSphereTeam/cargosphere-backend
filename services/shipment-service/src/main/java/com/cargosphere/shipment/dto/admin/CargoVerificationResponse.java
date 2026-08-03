package com.cargosphere.shipment.dto.admin;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import io.swagger.v3.oas.annotations.media.Schema;
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
        name = "CargoVerificationResponse",
        description = "Result of saving or confirming shipment cargo verification"
)
public class CargoVerificationResponse {

    @Schema(
            description = "Shipment identifier",
            example = "10"
    )
    private Long shipmentId;

    @Schema(
            description = "Cargo verification action that was processed",
            example = "SAVE_DRAFT"
    )
    private CargoVerificationAction action;

    @Schema(
            description = "Current backend-controlled shipment processing stage",
            example = "CARGO_VERIFICATION"
    )
    private ProcessingStage processingStage;

    @Schema(
            description = "Stored cargo verification records"
    )
    private List<CargoVerificationItemResponse> items;
}