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
        name = "ProcessingReadinessResponse",
        description =
                "Backend-calculated shipment processing readiness"
)
public class ProcessingReadinessResponse {

    @Schema(
            description = "Shipment identifier",
            example = "101"
    )
    private Long shipmentId;

    @Schema(
            description = "Unique shipment number",
            example = "SHP-2026-00101"
    )
    private String shipmentNumber;

    @Schema(
            description = "Current shipment processing stage",
            example = "DOCUMENT_VERIFICATION"
    )
    private ProcessingStage processingStage;

    @Schema(
            description =
                    "Whether a valid container allocation exists",
            example = "true"
    )
    private boolean containerReady;

    @Schema(
            description =
                    "Whether all shipment cargo has been confirmed",
            example = "true"
    )
    private boolean cargoReady;

    @Schema(
            description =
                    "Whether all mandatory documents are verified",
            example = "false"
    )
    private boolean documentsReady;

    @Schema(
            description =
                    "Whether a valid paid payment exists",
            example = "false"
    )
    private boolean paymentReady;

    @Schema(
            description =
                    "Whether every requirement for eBill generation is satisfied",
            example = "false"
    )
    private boolean ebillReady;

    @Schema(
            description =
                    "Reasons currently blocking further processing"
    )
    private List<String> blockingReasons;
}
