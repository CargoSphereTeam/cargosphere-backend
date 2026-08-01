package com.cargosphere.documentservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(
        name = "CreateDocumentRequest",
        description =
                "Request body used to create a shipment document checklist entry"
)
public class CreateDocumentRequest {

    @NotNull(message = "Shipment ID is required")
    @Positive(message = "Shipment ID must be positive")
    @Schema(
            description = "Shipment database ID",
            example = "1001",
            minimum = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long shipmentId;

    @NotBlank(message = "Document type is required")
    @Size(
            max = 100,
            message = "Document type cannot exceed 100 characters"
    )
    @Schema(
            description = "Document checklist type",
            example = "COMMERCIAL_INVOICE",
            maxLength = 100,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String documentType;

    @NotNull(message = "Required flag is required")
    @Schema(
            description =
                    "Whether this document is mandatory for the shipment",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Boolean required;

    @Size(
            max = 500,
            message = "Remarks cannot exceed 500 characters"
    )
    @Schema(
            description = "Optional remarks about the document",
            example = "Required for customs clearance",
            maxLength = 500
    )
    private String remarks;
}
