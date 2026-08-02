package com.cargosphere.shipment.dto.ebill;

import com.cargosphere.shipment.dto.ebill.snapshot.EbillClientSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillConfirmedCargoSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillContainerAllocationSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillDocumentSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillEventSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillOriginalCargoSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillPaymentSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillReadinessSnapshot;
import com.cargosphere.shipment.dto.ebill.snapshot.EbillShipmentSnapshot;
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
        name = "EbillPreviewResponse",
        description =
                "Live shipment information displayed before immutable eBill generation"
)
public class EbillPreviewResponse {

    @Schema(
            description = "Shipment information"
    )
    private EbillShipmentSnapshot shipment;

    @Schema(
            description = "Client information retrieved from auth-service"
    )
    private EbillClientSnapshot client;

    @Schema(
            description =
                    "Original cargo information submitted for the shipment"
    )
    private List<EbillOriginalCargoSnapshot> originalCargo;

    @Schema(
            description =
                    "Administrator-confirmed cargo information"
    )
    private List<EbillConfirmedCargoSnapshot> confirmedCargo;

    @Schema(
            description =
                    "Container allocations retrieved from container-service"
    )
    private List<EbillContainerAllocationSnapshot>
            containerAllocations;

    @Schema(
            description =
                    "Shipment documents retrieved from document-service"
    )
    private List<EbillDocumentSnapshot> documents;

    @Schema(
            description =
                    "Shipment payments retrieved from payment-service"
    )
    private List<EbillPaymentSnapshot> payments;

    @Schema(
            description = "Shipment event history"
    )
    private List<EbillEventSnapshot> shipmentEvents;

    @Schema(
            description =
                    "Backend-calculated eBill generation readiness"
    )
    private EbillReadinessSnapshot readiness;
}
