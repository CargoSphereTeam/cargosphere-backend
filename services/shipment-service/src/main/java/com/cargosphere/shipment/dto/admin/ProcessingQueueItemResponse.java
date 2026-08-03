package com.cargosphere.shipment.dto.admin;

import com.cargosphere.shipment.entity.enums.ProcessingStage;
import com.cargosphere.shipment.entity.enums.ShipmentStatus;
import com.cargosphere.shipment.entity.enums.ShipmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
        name = "ProcessingQueueItemResponse",
        description = "One shipment displayed in the administrator processing queue"
)
public class ProcessingQueueItemResponse {

    @Schema(
            description = "Shipment database identifier",
            example = "10"
    )
    private Long shipmentId;

    @Schema(
            description = "Unique shipment number",
            example = "SHP-2026-00010"
    )
    private String shipmentNumber;

    @Schema(
            description = "Client user who created the shipment",
            example = "100"
    )
    private Long clientUserId;

    @Schema(
            description = "Shipment origin",
            example = "Mumbai"
    )
    private String originLocation;

    @Schema(
            description = "Shipment destination",
            example = "Pune"
    )
    private String destinationLocation;

    @Schema(
            description = "Shipment transportation type"
    )
    private ShipmentType shipmentType;

    @Schema(
            description = "Current shipment business status"
    )
    private ShipmentStatus shipmentStatus;

    @Schema(
            description = "Current administrator processing stage",
            example = "PENDING_ADMIN_REVIEW"
    )
    private ProcessingStage processingStage;

    @Schema(
            description = "Expected pickup date"
    )
    private LocalDate expectedPickupDate;

    @Schema(
            description = "Expected delivery date"
    )
    private LocalDate expectedDeliveryDate;

    @Schema(
            description = "Time when administrative processing started"
    )
    private OffsetDateTime processingStartedAt;

    @Schema(
            description = "Time when administrative processing completed"
    )
    private OffsetDateTime processingCompletedAt;

    @Schema(
            description = "Time when the shipment was created"
    )
    private OffsetDateTime createdAt;

    @Schema(
            description = "Time when the shipment was last updated"
    )
    private OffsetDateTime updatedAt;
}