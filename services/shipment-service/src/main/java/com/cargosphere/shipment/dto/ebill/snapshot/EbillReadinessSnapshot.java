package com.cargosphere.shipment.dto.ebill.snapshot;

import com.cargosphere.shipment.entity.enums.ProcessingStage;

import java.util.List;

public record EbillReadinessSnapshot(
        ProcessingStage processingStage,
        boolean containerReady,
        boolean cargoReady,
        boolean documentsReady,
        boolean paymentReady,
        boolean ebillReady,
        List<String> blockingReasons
) {
    public EbillReadinessSnapshot {
        blockingReasons =
                blockingReasons == null
                        ? List.of()
                        : List.copyOf(blockingReasons);
    }
}
