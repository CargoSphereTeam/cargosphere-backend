package com.cargosphere.shipment.integration.document;

import java.util.List;

public record ShipmentDocumentReadinessResponse(
        Long shipmentId,
        long totalDocuments,
        long requiredDocuments,
        long verifiedRequiredDocuments,
        long notApplicableRequiredDocuments,
        long blockingRequiredDocuments,
        boolean allMandatoryDocumentsResolved,
        List<String> blockingDocumentTypes
) {

    public ShipmentDocumentReadinessResponse {
        blockingDocumentTypes =
                blockingDocumentTypes == null
                        ? List.of()
                        : List.copyOf(blockingDocumentTypes);
    }
}