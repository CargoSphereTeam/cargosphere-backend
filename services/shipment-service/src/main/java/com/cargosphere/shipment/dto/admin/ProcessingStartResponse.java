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
        name = "ProcessingStartResponse",
        description = "Result returned after administrator starts shipment processing"
)
public class ProcessingStartResponse {

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
            description = "Current shipment processing stage",
            example = "CONTAINER_ALLOCATION"
    )
    private ProcessingStage processingStage;

    @Schema(
            description = "Date and time when administrative processing started"
    )
    private OffsetDateTime processingStartedAt;
}