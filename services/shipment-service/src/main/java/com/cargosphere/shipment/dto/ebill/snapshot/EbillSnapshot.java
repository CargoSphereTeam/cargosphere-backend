package com.cargosphere.shipment.dto.ebill.snapshot;

import java.time.OffsetDateTime;
import java.util.List;

public record EbillSnapshot(
        String schemaVersion,
        String ebillNumber,
        Integer ebillVersion,
        OffsetDateTime generatedAt,
        Long generatedBy,
        EbillShipmentSnapshot shipment,
        EbillClientSnapshot client,
        List<EbillOriginalCargoSnapshot> originalCargo,
        List<EbillConfirmedCargoSnapshot> confirmedCargo,
        List<EbillContainerAllocationSnapshot> containerAllocations,
        List<EbillDocumentSnapshot> documents,
        List<EbillPaymentSnapshot> payments,
        List<EbillEventSnapshot> shipmentEvents,
        EbillReadinessSnapshot readiness
) {
    public EbillSnapshot {
        originalCargo =
                immutableList(originalCargo);

        confirmedCargo =
                immutableList(confirmedCargo);

        containerAllocations =
                immutableList(containerAllocations);

        documents =
                immutableList(documents);

        payments =
                immutableList(payments);

        shipmentEvents =
                immutableList(shipmentEvents);
    }

    private static <T> List<T> immutableList(
            List<T> values
    ) {
        return values == null
                ? List.of()
                : List.copyOf(values);
    }
}
