package com.cargosphere.documentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateDocumentRequest {

    @NotNull(message = "Shipment ID is required")
    @Positive(message = "Shipment ID must be positive")
    private Long shipmentId;

    @NotBlank(message = "Document type is required")
    @Size(max = 100, message = "Document type cannot exceed 100 characters")
    private String documentType;

    @NotNull(message = "Required flag is required")
    private Boolean required;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}