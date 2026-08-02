package com.cargosphere.shipment.dto.admin;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProcessingContinueResponse",
        description = "Result returned after shipment processing advances to the next stage"
)
public class ProcessingContinueResponse {

    @Schema(
            description = "Shipment identifier",
            example = "10"
    )
    private Long shipmentId;

    @Schema(
            description = "Unique shipment number",
            example = "SHP-2026-00010"
    )
    private String shipmentNumber;

    @Schema(
            description = "Processing stage before advancement",
            example = "DOCUMENT_VERIFICATION"
    )
    private ProcessingStage previousStage;

    @Schema(
            description = "Current processing stage after advancement",
            example = "PAYMENT_CONFIRMATION"
    )
    private ProcessingStage processingStage;

    @Schema(
            description = "Date and time when processing advanced"
    )
    private OffsetDateTime advancedAt;
}
