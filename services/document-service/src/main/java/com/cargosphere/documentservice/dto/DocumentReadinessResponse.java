package com.cargosphere.documentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "DocumentReadinessResponse",
        description = "Document verification readiness for a shipment"
)
public record DocumentReadinessResponse(

        @Schema(example = "101")
        Long shipmentId,

        @Schema(example = "6")
        long totalDocuments,

        @Schema(example = "5")
        long requiredDocuments,

        @Schema(example = "4")
        long verifiedRequiredDocuments,

        @Schema(example = "1")
        long notApplicableRequiredDocuments,

        @Schema(example = "0")
        long blockingRequiredDocuments,

        @Schema(example = "true")
        boolean allMandatoryDocumentsResolved,

        @Schema(
                description = "Required document types preventing progression"
        )
        List<String> blockingDocumentTypes
) {
}