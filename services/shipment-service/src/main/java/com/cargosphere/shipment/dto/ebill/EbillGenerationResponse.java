package com.cargosphere.shipment.dto.ebill;

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
        name = "EbillGenerationResponse",
        description =
                "Metadata of the immutable eBill generated for a shipment"
)
public class EbillGenerationResponse {

    @Schema(description = "Shipment database identifier")
    private Long shipmentId;

    @Schema(description = "Human-readable shipment number")
    private String shipmentNumber;

    @Schema(description = "Unique generated eBill number")
    private String ebillNumber;

    @Schema(description = "Immutable eBill version")
    private Integer ebillVersion;

    @Schema(description = "UTC timestamp when the eBill was generated")
    private OffsetDateTime generatedAt;

    @Schema(description = "Administrator user ID that generated the eBill")
    private Long generatedBy;

    @Schema(description = "Current shipment-processing stage")
    private ProcessingStage processingStage;
}
